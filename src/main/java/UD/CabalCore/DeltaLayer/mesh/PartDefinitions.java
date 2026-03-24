package UD.CabalCore.DeltaLayer.mesh;

public final class PartDefinitions {

    private PartDefinitions() {
    }

    // UV origins are for OUTER LAYER boxes on 64x64 skins

    public static final PartDefinition HEAD = new PartDefinition(
            32, 0,
            8, 8, 8,
            -4.0F, -8.0F, -4.0F,
            0.085F
    );

    public static final PartDefinition BODY = new PartDefinition(
            16, 32,
            8, 12, 4,
            -4.0F, 0.0F, -2.0F,
            0.05F
    );

    public static final PartDefinition RIGHT_LEG = new PartDefinition(
            0, 32,
            4, 12, 4,
            -2.0F, 0.0F, -2.0F,
            0.05F
    );

    public static final PartDefinition LEFT_LEG = new PartDefinition(
            0, 48,
            4, 12, 4,
            -2.0F, 0.0F, -2.0F,
            0.05F
    );

    public static PartDefinition rightArm(boolean slim) {
        if (slim) {
            return new PartDefinition(
                    40, 32,
                    3, 12, 4,
                    -2.0F, -2.0F, -2.0F,
                    0.05F
            );
        }

        return new PartDefinition(
                40, 32,
                4, 12, 4,
                -3.0F, -2.0F, -2.0F,
                0.05F
        );
    }

    public static PartDefinition leftArm(boolean slim) {
        if (slim) {
            return new PartDefinition(
                    48, 48,
                    3, 12, 4,
                    -1.0F, -2.0F, -2.0F,
                    0.05F
            );
        }

        return new PartDefinition(
                48, 48,
                4, 12, 4,
                -1.0F, -2.0F, -2.0F,
                0.05F
        );
    }
}