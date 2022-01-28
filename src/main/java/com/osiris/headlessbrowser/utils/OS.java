package com.osiris.headlessbrowser.utils;

public class OS {
    /**
     * The current operating system type.
     */
    public static Type TYPE;
    /**
     * The current operating systems' architecture.
     */
    public static Arch ARCH;

    static {
        // First set the details we need
        // Start by setting the operating systems architecture type
        String actualOsArchitecture = System.getProperty("os.arch").toLowerCase();
        for (Arch type :
                Arch.values()) {
            if (actualOsArchitecture.equals(type.toString().toLowerCase())) // Not comparing the actual names because the enum has more stuff matching one name
                ARCH = type;
        }
        if (ARCH == null) {
            // Do another check.
            // On windows it can be harder to detect the right architecture that's why we do the stuff below:
            // Source: https://stackoverflow.com/questions/4748673/how-can-i-check-the-bitness-of-my-os-using-java-j2se-not-os-arch/5940770#5940770
            String arch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
            boolean is64 = arch != null && arch.endsWith("64")
                    || wow64Arch != null && wow64Arch.endsWith("64"); // Otherwise its 32bit
            if (is64)
                ARCH = Arch.X64;
            else
                ARCH = Arch.X32;
            // "The current operating systems architecture '" + actualOsArchitecture +"' was not found in the architectures list '" + Arrays.toString(OperatingSystemArchitectureType.values()) + "'." +" Defaulting to '" + osArchitectureType + "'.");
        }

        // Set the operating systems type

        String actualOsType = System.getProperty("os.name").toLowerCase();
        if (actualOsType.contains("alpine"))
            TYPE = Type.ALPINE_LINUX;
        if (actualOsType.contains("win"))
            TYPE = Type.WINDOWS;
        else if (actualOsType.contains("mac"))
            TYPE = Type.MAC;
        else if (actualOsType.contains("aix"))
            TYPE = Type.AIX;
        else if (actualOsType.contains("nix")
                || actualOsType.contains("nux"))
            TYPE = Type.LINUX;
        else if (actualOsType.contains("sunos"))
            TYPE = Type.SOLARIS;
        else {
            TYPE = Type.LINUX;
            //"The current operating system '" + actualOsType + "' was not found in the supported operating systems list. Defaulting to '" + OperatingSystemType.LINUX.name() + "'.");
        }
    }

    public static boolean isWindows() {
        return TYPE.equals(Type.WINDOWS);
    }

    public static boolean isMac() {
        return TYPE.equals(Type.MAC);
    }

    public static boolean isSolaris() {
        return TYPE.equals(Type.SOLARIS);
    }

    public static boolean isLinux() {
        return TYPE.equals(Type.LINUX);
    }

    /**
     * Stuff like Linux, Solaris, MacOS, are all systems based on Unix.
     */
    public static boolean isBasedOnUnix() {
        return TYPE.equals(Type.LINUX) || TYPE.equals(Type.MAC) || TYPE.equals(Type.SOLARIS) || TYPE.equals(Type.AIX) || TYPE.equals(Type.ALPINE_LINUX);
    }

    public enum Arch {
        X64("x64", "64"),
        X86("x86", "86"),
        X32("x32", "32"),
        PPC64("ppc64", "x64", "64"),
        PPC64LE("ppc64le", "x64", "64"),
        S390X("s390x"),
        AARCH64("aarch64", "x64", "64"),
        ARM("arm"),
        SPARCV9("sparcv9"),
        RISCV64("riscv64", "x64", "64"),
        // x64 with alternative names:
        AMD64("x64", "64"),
        X86_64("x64", "64"),
        // x32 with alternative names:
        I386("x32", "32");

        /**
         * Alternative names.
         */
        public final String[] altNames;

        Arch(String... altNames) {
            this.altNames = altNames;
        }
    }

    public enum Type {
        LINUX("linux"),
        WINDOWS("windows"),
        MAC("mac"),
        SOLARIS("solaris"),
        AIX("aix"),
        ALPINE_LINUX("alpine-linux");

        public final String name;

        Type(String name) {
            this.name = name;
        }
    }
}
