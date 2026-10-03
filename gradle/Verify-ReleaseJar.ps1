param([Parameter(Mandatory=$true)][string]$Jar,[Parameter(Mandatory=$true)][ValidateSet('forge','neoforge')][string]$Loader,[string]$ModId='adventuresystems',[int]$ClassVersion=65)
$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip=[IO.Compression.ZipFile]::OpenRead($Jar)
try {
    function Read-Entry([string]$name) {
        $entry=$zip.GetEntry($name)
        if (!$entry) { throw "Missing JAR entry: $name" }
        $reader=New-Object IO.StreamReader($entry.Open())
        try { return $reader.ReadToEnd() } finally { $reader.Dispose() }
    }
    $classes=@($zip.Entries | Where-Object { $_.FullName -like '*.class' })
    if (!$classes.Count) { throw 'Release JAR has no classes' }
    foreach($entry in $classes) {
        $stream=$entry.Open()
        try {
            $bytes=New-Object byte[] 8
            if ($stream.Read($bytes,0,8) -ne 8 -or ($bytes[6]*256+$bytes[7]) -ne $ClassVersion) {
                throw "Wrong class version in $($entry.FullName)"
            }
        } finally { $stream.Dispose() }
    }
    $metadata=if($Loader -eq 'forge') {'META-INF/mods.toml'} else {'META-INF/neoforge.mods.toml'}
    $other=if($Loader -eq 'forge') {'META-INF/neoforge.mods.toml'} else {'META-INF/mods.toml'}
    $toml=Read-Entry $metadata
    if($zip.GetEntry($other)) { throw "Wrong loader metadata also packaged: $other" }
    if($toml -match '\$\{') { throw 'Unexpanded metadata placeholders' }
    if($toml -notmatch 'versionRange="\[26\.10\.3,\)"') { throw 'KineticCore dependency range changed' }
    $pack=(Read-Entry 'pack.mcmeta') | ConvertFrom-Json
    if (!$pack.pack.pack_format) { throw 'Invalid resource pack format' }
    $manifest=Read-Entry 'META-INF/MANIFEST.MF'
    $configs=@($zip.Entries | Where-Object { $_.Name -like '*.mixins.json' })

    foreach($entry in $configs) {
        $config=(Read-Entry $entry.FullName) | ConvertFrom-Json
        if($Loader -eq 'forge') {
            if($config.compatibilityLevel -ne 'JAVA_17' -or !$config.refmap) { throw "Invalid Forge Mixin config: $($entry.Name)" }
            if(!$zip.GetEntry($config.refmap)) { throw 'Forge refmap is missing' }
            if($manifest -notmatch 'MixinConfigs:') { throw 'Forge MixinConfigs manifest attribute missing' }
        } else {
            if($config.refmap -or $config.compatibilityLevel -ne ('JAVA_'+($ClassVersion-44))) { throw "Invalid NeoForge Mixin config: $($entry.Name)" }
            if(!$toml.Contains($entry.Name)) { throw "NeoForge TOML does not register $($entry.Name)" }
        }
    }
    if($zip.Entries | Where-Object { $_.FullName -like 'dev/xyat/adventurevalidation/*' -or $_.FullName -like '*AdventureTextTestAgent*' }) { throw 'Test fixture or agent leaked into release' }
    if($zip.GetEntry("assets/$ModId/text-layouts.json")) { throw 'Obsolete generated text layout leaked into release' }
    $languageKeys=@{}
    foreach($locale in @('en_us','zh_cn')) {
        $language=(Read-Entry "assets/$ModId/lang/$locale.json") | ConvertFrom-Json
        $languageKeys[$locale]=@($language.PSObject.Properties.Name)
        foreach($entry in $language.PSObject.Properties) {
            if($entry.Value -isnot [string]) { throw "Non-string language value: $locale/$($entry.Name)" }
            if($entry.Name -cmatch '\.formatted(?:\.original|\.part\.\d+)?$') { throw "Generated language key: $locale/$($entry.Name)" }
        }
    }
    if(Compare-Object $languageKeys.en_us $languageKeys.zh_cn -CaseSensitive) { throw 'Release English/Chinese language keys differ' }
    [PSCustomObject]@{Jar=[IO.Path]::GetFileName($Jar);Classes=$classes.Count;ClassVersion=$ClassVersion;MixinConfigs=$configs.Count;LanguageKeys=$languageKeys.en_us.Count;Result='PASS'} | ConvertTo-Json -Compress
} finally { $zip.Dispose() }
