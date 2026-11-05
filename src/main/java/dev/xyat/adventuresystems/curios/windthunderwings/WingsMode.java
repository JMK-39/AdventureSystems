package dev.xyat.adventuresystems.curios.windthunderwings;

/** 风雷翅的三种飞行状态；序号用于网络同步。 */
public enum WingsMode {
    /** 普通飞行：翅膀可见并扇动。 */
    NORMAL,
    /** 遁光加速：化作遁光，人物不可见。 */
    BOOST,
    /** 燃血：消耗生命换取短时间的极速，血色遁光。 */
    BURN;

    public static WingsMode of(int ordinal) {
        WingsMode[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : NORMAL;
    }
}
