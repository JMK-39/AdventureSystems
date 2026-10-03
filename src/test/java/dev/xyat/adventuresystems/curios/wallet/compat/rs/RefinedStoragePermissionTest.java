//? if >=1.21 {
/*package dev.xyat.adventuresystems.curios.wallet.compat.rs;
public class RefinedStoragePermissionTest {
 @org.junit.jupiter.api.BeforeAll static void bootstrap()throws Exception {if(net.neoforged.fml.loading.LoadingModList.get()==null)net.neoforged.fml.loading.LoadingModList.of(java.util.List.of(),java.util.List.of(),java.util.List.of(),java.util.List.of(),java.util.Map.of());net.neoforged.fml.loading.FMLPaths.loadAbsolutePaths(java.nio.file.Files.createTempDirectory("rs-permission-tests-"));net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
 @org.junit.jupiter.api.Test void extractionRechecksRevokedPermissionBeforeStorageAccess()throws Exception {
    var field=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);var unsafe=(sun.misc.Unsafe)field.get(null);
    var player=(net.minecraft.server.level.ServerPlayer)unsafe.allocateInstance(net.minecraft.server.level.ServerPlayer.class);
    var profile=net.minecraft.world.entity.player.Player.class.getDeclaredField("gameProfile");profile.setAccessible(true);profile.set(player,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"PermissionTest"));
    check(player);
 }
 public static void check(net.minecraft.server.level.ServerPlayer player)throws Exception {
    var allowed=new java.util.concurrent.atomic.AtomicBoolean(true);var calls=new java.util.concurrent.atomic.AtomicInteger();
    var security=(com.refinedmods.refinedstorage.common.api.security.PlatformSecurityNetworkComponent)java.lang.reflect.Proxy.newProxyInstance(RefinedStoragePermissionTest.class.getClassLoader(),new Class[]{com.refinedmods.refinedstorage.common.api.security.PlatformSecurityNetworkComponent.class},(p,m,a)->m.getName().equals("isAllowed")?allowed.get():false);
    var storage=(com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent)java.lang.reflect.Proxy.newProxyInstance(RefinedStoragePermissionTest.class.getClassLoader(),new Class[]{com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent.class},(p,m,a)->{if(m.getName().equals("extract")){calls.incrementAndGet();return (Long)a[1];}return null;});
    var network=(com.refinedmods.refinedstorage.api.network.Network)java.lang.reflect.Proxy.newProxyInstance(RefinedStoragePermissionTest.class.getClassLoader(),new Class[]{com.refinedmods.refinedstorage.api.network.Network.class},(p,m,a)->a[0]==com.refinedmods.refinedstorage.common.api.security.PlatformSecurityNetworkComponent.class?security:storage);
    var method=RefinedStorageCompat.class.getDeclaredMethod("extractFromNetwork",net.minecraft.server.level.ServerPlayer.class,com.refinedmods.refinedstorage.api.network.Network.class,net.minecraft.world.item.ItemStack.class,long.class,boolean.class);method.setAccessible(true);
    var target=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND);
    org.junit.jupiter.api.Assertions.assertEquals(4L,method.invoke(null,player,network,target,4L,true));
    allowed.set(false);calls.set(0);
    org.junit.jupiter.api.Assertions.assertEquals(0L,method.invoke(null,player,network,target,4L,true),"Revoked EXTRACT must deny simulated counts");
    org.junit.jupiter.api.Assertions.assertEquals(0L,method.invoke(null,player,network,target,4L,false),"Revoked EXTRACT must deny execution");
    org.junit.jupiter.api.Assertions.assertEquals(0,calls.get(),"Denied player must never reach underlying storage");
 }
}

*///?}
