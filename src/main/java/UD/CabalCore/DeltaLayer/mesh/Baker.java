package UD.CabalCore.DeltaLayer.mesh;

import com.mojang.blaze3d.platform.NativeImage;
import UD.CabalCore.DeltaLayer.cache.ImgCache;
import UD.CabalCore.DeltaLayer.cache.MeshKey;
import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public final class Baker {

    private static final float BASE_DEPTH = 0.42F;

    private Baker() {
    }

    public static MeshBundle bake(MeshKey key) {
        NativeImage image = ImgCache.get(key.skin());

        if (image == null) {
            return empty();
        }

        if (image.getWidth() != 64 || image.getHeight() != 64) {
            return empty();
        }

        boolean slim = key.slim();

        return new MeshBundle(
                bakeHead(image),
                bakeCuboid(image, PartDefinitions.BODY),
                bakeCuboid(image, PartDefinitions.leftArm(slim)),
                bakeCuboid(image, PartDefinitions.rightArm(slim)),
                bakeCuboid(image, PartDefinitions.LEFT_LEG),
                bakeCuboid(image, PartDefinitions.RIGHT_LEG)
        );
    }

    private static MeshBundle empty() {
        PartMesh empty = new PartMesh(List.of());
        return new MeshBundle(empty, empty, empty, empty, empty, empty);
    }

    private static float partScale(PartDefinition def) {
        if (def == PartDefinitions.BODY) {
            return 1.00F;
        }
        return 0.95F;
    }

    private static float faceScaleFrontBack(PartDefinition def) {
        return 1.00F;
    }

    private static float faceScaleSide(PartDefinition def) {
        return 0.85F;
    }

    private static float faceScaleTopBottom(PartDefinition def) {
        return 0.70F;
    }

// =========================================================
// HEAD - 11th rewrite occupancy-grid try
// =========================================================

    // These are the visually proven front values you already found.
    private static final float HEAD_FRONT_DEPTH = 0.5f;
    private static final float HEAD_FRONT_UP = 0.08f;
    private static final float HEAD_FRONT_DOWN = 0.08f;
    private static final float HEAD_FRONT_WIDTH_EXTRA = 0.10f;
    private static final float HEAD_FRONT_HEIGHT_EXTRA = 0.06f;
    private static final float HEAD_FRONT_VERTICAL_EXTRA = 0.50f;

    // Keep back matched to front envelope for now.
    private static final float HEAD_BACK_DEPTH = 0.5f;
    private static final float HEAD_BACK_UP = 0.08f;
    private static final float HEAD_BACK_DOWN = 0.08f;
    private static final float HEAD_BACK_WIDTH_EXTRA = 0.10f;
    private static final float HEAD_BACK_HEIGHT_EXTRA = 0.06f;
    private static final float HEAD_BACK_VERTICAL_EXTRA = 0.50f;

    // Side shell thickness.
    private static final float HEAD_SIDE_DEPTH = 0.5f;

    // Top / bottom thickness.
    private static final float HEAD_TOP_DEPTH = 0.5f;
    private static final float HEAD_BOTTOM_DEPTH = 0.5f;

    private static final float HEAD_EPS = 0.0005f;
    private static final float HEAD_SEAM_EPS = 0.0015f;

    // Optional side insets if you want to mimic the slight 3DSL asymmetry later.
// Keep at 0 for now until needed.
    private static final float HEAD_LEFT_INSET = 0.0f;
    private static final float HEAD_RIGHT_INSET = 0.0f;

    private enum Dir {
        NEG_X, POS_X,
        NEG_Y, POS_Y,
        NEG_Z, POS_Z
    }

    private static final class CellKey {
        final int x, y, z;

        CellKey(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CellKey other)) return false;
            return x == other.x && y == other.y && z == other.z;
        }

        @Override
        public int hashCode() {
            int h = x;
            h = 31 * h + y;
            h = 31 * h + z;
            return h;
        }
    }

    private static final class FaceSample {
        final int u, v;

        FaceSample(int u, int v) {
            this.u = u;
            this.v = v;
        }
    }

    private static final class ShellCell {
        final EnumMap<Dir, FaceSample> outward = new EnumMap<>(Dir.class);
    }

    private static PartMesh bakeHead(NativeImage skin) {
        PartDefinition part = PartDefinitions.HEAD;

        Map<CellKey, ShellCell> shell = new HashMap<>(256);

        collectFront(shell, skin);
        collectBack(shell, skin);
        collectLeft(shell, skin);
        collectRight(shell, skin);
        collectTop(shell, skin);
        collectBottom(shell, skin);

        List<BakedQuad3D> quads = new ArrayList<>(256);

        for (Map.Entry<CellKey, ShellCell> entry : shell.entrySet()) {
            CellKey key = entry.getKey();
            ShellCell cell = entry.getValue();

            for (Map.Entry<Dir, FaceSample> face : cell.outward.entrySet()) {
                emitOwnedSlab(quads, shell, part, key, face.getKey(), face.getValue());
            }
        }

        emitHeadSeamStrips(quads, shell, part);
        emitHeadCornerBoxes(quads, shell, part);

        return new PartMesh(quads);

    }

    private static void collectFront(Map<CellKey, ShellCell> shell, NativeImage skin) {
        for (int v = 0; v < 8; v++) {
            for (int u = 0; u < 8; u++) {
                if (!opaque(skin, 40 + u, 8 + v)) continue;
                put(shell, u, v, -1, Dir.NEG_Z, 40 + u, 8 + v);

            }
        }
    }

    private static boolean hasFace(Map<CellKey, ShellCell> shell, int x, int y, int z, Dir dir) {
        ShellCell cell = shell.get(new CellKey(x, y, z));
        return cell != null && cell.outward.containsKey(dir);
    }

    private static void collectBack(Map<CellKey, ShellCell> shell, NativeImage skin) {
        for (int v = 0; v < 8; v++) {
            for (int u = 0; u < 8; u++) {
                if (!opaque(skin, 56 + u, 8 + v)) continue;
                put(shell, 7 - u, v, 8, Dir.POS_Z, 56 + u, 8 + v);
            }
        }
    }

    private static void collectLeft(Map<CellKey, ShellCell> shell, NativeImage skin) {
        for (int v = 0; v < 8; v++) {
            for (int u = 0; u < 8; u++) {
                if (!opaque(skin, 32 + u, 8 + v)) continue;
                put(shell, -1, v, 7 - u, Dir.NEG_X, 32 + u, 8 + v);
            }
        }
    }

    private static void collectRight(Map<CellKey, ShellCell> shell, NativeImage skin) {
        for (int v = 0; v < 8; v++) {
            for (int u = 0; u < 8; u++) {
                if (!opaque(skin, 48 + u, 8 + v)) continue;
                put(shell, 8, v, u, Dir.POS_X, 48 + u, 8 + v);
            }
        }
    }

    private static void collectTop(Map<CellKey, ShellCell> shell, NativeImage skin) {
        for (int v = 0; v < 8; v++) {
            for (int u = 0; u < 8; u++) {
                if (!opaque(skin, 40 + u, v)) continue;
                put(shell, u, -1, 7 - v, Dir.NEG_Y, 40 + u, v);
            }
        }
    }

    private static void collectBottom(Map<CellKey, ShellCell> shell, NativeImage skin) {
        for (int v = 0; v < 8; v++) {
            for (int u = 0; u < 8; u++) {
                if (!opaque(skin, 48 + u, v)) continue;
                put(shell, u, 8, v, Dir.POS_Y, 48 + u, v);
            }
        }
    }

    private static void put(Map<CellKey, ShellCell> shell,
                            int x, int y, int z,
                            Dir dir,
                            int u, int v) {
        CellKey key = new CellKey(x, y, z);
        ShellCell cell = shell.computeIfAbsent(key, k -> new ShellCell());
        cell.outward.put(dir, new FaceSample(u, v));
    }

    private static void emitOwnedSlab(List<BakedQuad3D> quads,
                                      Map<CellKey, ShellCell> shell,
                                      PartDefinition part,
                                      CellKey key,
                                      Dir dir,
                                      FaceSample uv) {

        float x0 = part.minX;
        float x1 = part.maxX();
        float y0 = part.minY;
        float y1 = part.maxY();
        float z0 = part.minZ;
        float z1 = part.maxZ();

        float px = (x1 - x0) / 8.0f;
        float py = (y1 - y0) / 8.0f;
        float pz = (z1 - z0) / 8.0f;

        float frontMinX = x0 - HEAD_SIDE_DEPTH - HEAD_FRONT_WIDTH_EXTRA;
        float frontMaxX = x1 + HEAD_SIDE_DEPTH + HEAD_FRONT_WIDTH_EXTRA;
        float frontMinY = y0 - HEAD_FRONT_UP - HEAD_FRONT_HEIGHT_EXTRA - HEAD_FRONT_VERTICAL_EXTRA;
        float frontMaxY = y1 + HEAD_FRONT_DOWN + HEAD_FRONT_HEIGHT_EXTRA + HEAD_FRONT_VERTICAL_EXTRA;
        float frontPx = (frontMaxX - frontMinX) / 8.0f;
        float frontPy = (frontMaxY - frontMinY) / 8.0f;

        float backMinX = x0 - HEAD_SIDE_DEPTH - HEAD_BACK_WIDTH_EXTRA;
        float backMaxX = x1 + HEAD_SIDE_DEPTH + HEAD_BACK_WIDTH_EXTRA;
        float backMinY = y0 - HEAD_BACK_UP - HEAD_BACK_HEIGHT_EXTRA - HEAD_BACK_VERTICAL_EXTRA;
        float backMaxY = y1 + HEAD_BACK_DOWN + HEAD_BACK_HEIGHT_EXTRA + HEAD_BACK_VERTICAL_EXTRA;
        float backPx = (backMaxX - backMinX) / 8.0f;
        float backPy = (backMaxY - backMinY) / 8.0f;

        float sideMinY = frontMinY;
        float sideMaxY = frontMaxY;
        float sidePy = (sideMaxY - sideMinY) / 8.0f;

        float sideMinZ = z0 - HEAD_FRONT_DEPTH;
        float sideMaxZ = z1 + HEAD_BACK_DEPTH;
        float sidePz = (sideMaxZ - sideMinZ) / 8.0f;

        float outerMinX = x0 - HEAD_SIDE_DEPTH - HEAD_FRONT_WIDTH_EXTRA;
        float outerMaxX = x1 + HEAD_SIDE_DEPTH + HEAD_FRONT_WIDTH_EXTRA;
        float outerMinZ = z0 - HEAD_FRONT_DEPTH;
        float outerMaxZ = z1 + HEAD_BACK_DEPTH;

        float topPx = (outerMaxX - outerMinX) / 8.0f;
        float topPz = (outerMaxZ - outerMinZ) / 8.0f;

        switch (dir) {
            case NEG_Z -> {
                int cx = key.x;
                int cy = key.y;

                float ax0 = frontMinX + cx * frontPx;
                float ax1 = ax0 + frontPx;
                float ay0 = frontMinY + cy * frontPy;
                float ay1 = ay0 + frontPy;
                float az0 = z0 - HEAD_FRONT_DEPTH;
                float az1 = z0 - HEAD_EPS;

                addQuadFront(quads, ax0, ax1, ay0, ay1, az0, uv.u, uv.v);

                if (!hasFace(shell, cx - 1, cy, -1, Dir.NEG_Z))
                    addQuadLeft(quads, ax0, ay0, ay1, az0, az1, uv.u, uv.v);

                if (!hasFace(shell, cx + 1, cy, -1, Dir.NEG_Z))
                    addQuadRight(quads, ax1, ay0, ay1, az0, az1, uv.u, uv.v);

                if (!hasFace(shell, cx, cy - 1, -1, Dir.NEG_Z))
                    addQuadTop(quads, ax0, ax1, ay0, az0, az1, uv.u, uv.v);

                if (!hasFace(shell, cx, cy + 1, -1, Dir.NEG_Z))
                    addQuadBottom(quads, ax0, ax1, ay1, az0, az1, uv.u, uv.v);
            }

            case POS_Z -> {
                int cx = key.x;
                int cy = key.y;

                float ax0 = backMinX + cx * backPx;
                float ax1 = ax0 + backPx;
                float ay0 = backMinY + cy * backPy;
                float ay1 = ay0 + backPy;
                float az0 = z1 + HEAD_EPS;
                float az1 = z1 + HEAD_BACK_DEPTH;

                addQuadBack(quads, ax0, ax1, ay0, ay1, az1, uv.u, uv.v);

                if (!hasFace(shell, cx - 1, cy, 8, Dir.POS_Z))
                    addQuadLeft(quads, ax0, ay0, ay1, az0, az1, uv.u, uv.v);

                if (!hasFace(shell, cx + 1, cy, 8, Dir.POS_Z))
                    addQuadRight(quads, ax1, ay0, ay1, az0, az1, uv.u, uv.v);

                if (!hasFace(shell, cx, cy - 1, 8, Dir.POS_Z))
                    addQuadTop(quads, ax0, ax1, ay0, az0, az1, uv.u, uv.v);

                if (!hasFace(shell, cx, cy + 1, 8, Dir.POS_Z))
                    addQuadBottom(quads, ax0, ax1, ay1, az0, az1, uv.u, uv.v);
            }

            case NEG_X -> {
                int cy = key.y;
                int cz = key.z;

                float ax0 = x0 - HEAD_SIDE_DEPTH;
                float ax1 = x0 - HEAD_EPS;
                float ay0 = sideMinY + cy * sidePy;
                float ay1 = ay0 + sidePy;
                float az0 = sideMinZ + cz * sidePz;
                float az1 = az0 + sidePz;

                addQuadLeft(quads, ax0, ay0, ay1, az0, az1, uv.u, uv.v);

                if (!hasFace(shell, -1, cy, cz - 1, Dir.NEG_X))
                    addQuadFront(quads, ax0, ax1, ay0, ay1, az0, uv.u, uv.v);

                if (!hasFace(shell, -1, cy, cz + 1, Dir.NEG_X))
                    addQuadBack(quads, ax0, ax1, ay0, ay1, az1, uv.u, uv.v);

                if (!hasFace(shell, -1, cy - 1, cz, Dir.NEG_X))
                    addQuadTop(quads, ax0, ax1, ay0, az0, az1, uv.u, uv.v);

                if (!hasFace(shell, -1, cy + 1, cz, Dir.NEG_X))
                    addQuadBottom(quads, ax0, ax1, ay1, az0, az1, uv.u, uv.v);
            }

            case POS_X -> {
                int cy = key.y;
                int cz = key.z;

                float ax0 = x1 + HEAD_EPS;
                float ax1 = x1 + HEAD_SIDE_DEPTH;
                float ay0 = sideMinY + cy * sidePy;
                float ay1 = ay0 + sidePy;
                float az0 = sideMinZ + cz * sidePz;
                float az1 = az0 + sidePz;

                addQuadRight(quads, ax1, ay0, ay1, az0, az1, uv.u, uv.v);

                if (!hasFace(shell, 8, cy, cz - 1, Dir.POS_X))
                    addQuadFront(quads, ax0, ax1, ay0, ay1, az0, uv.u, uv.v);

                if (!hasFace(shell, 8, cy, cz + 1, Dir.POS_X))
                    addQuadBack(quads, ax0, ax1, ay0, ay1, az1, uv.u, uv.v);

                if (!hasFace(shell, 8, cy - 1, cz, Dir.POS_X))
                    addQuadTop(quads, ax0, ax1, ay0, az0, az1, uv.u, uv.v);

                if (!hasFace(shell, 8, cy + 1, cz, Dir.POS_X))
                    addQuadBottom(quads, ax0, ax1, ay1, az0, az1, uv.u, uv.v);
            }

            case NEG_Y -> {
                int cx = key.x;
                int cz = key.z;

                float ax0 = outerMinX + cx * topPx;
                float ax1 = ax0 + topPx;
                float ay0 = y0 - HEAD_TOP_DEPTH;
                float ay1 = y0 - HEAD_EPS;
                float az0 = outerMinZ + cz * topPz;
                float az1 = az0 + topPz;

                addQuadTop(quads, ax0, ax1, ay0, az0, az1, uv.u, uv.v);

                if (!hasFace(shell, cx - 1, -1, cz, Dir.NEG_Y))
                    addQuadLeft(quads, ax0, ay0, ay1, az0, az1, uv.u, uv.v);

                if (!hasFace(shell, cx + 1, -1, cz, Dir.NEG_Y))
                    addQuadRight(quads, ax1, ay0, ay1, az0, az1, uv.u, uv.v);

                if (!hasFace(shell, cx, -1, cz - 1, Dir.NEG_Y))
                    addQuadFront(quads, ax0, ax1, ay0, ay1, az0, uv.u, uv.v);

                if (!hasFace(shell, cx, -1, cz + 1, Dir.NEG_Y))
                    addQuadBack(quads, ax0, ax1, ay0, ay1, az1, uv.u, uv.v);
            }

            case POS_Y -> {
                int cx = key.x;
                int cz = key.z;

                float ax0 = outerMinX + cx * topPx;
                float ax1 = ax0 + topPx;
                float ay0 = y1 + HEAD_EPS;
                float ay1 = y1 + HEAD_BOTTOM_DEPTH;
                float az0 = outerMinZ + cz * topPz;
                float az1 = az0 + topPz;

                addQuadBottom(quads, ax0, ax1, ay1, az0, az1, uv.u, uv.v);

                if (!hasFace(shell, cx - 1, 8, cz, Dir.POS_Y))
                    addQuadLeft(quads, ax0, ay0, ay1, az0, az1, uv.u, uv.v);

                if (!hasFace(shell, cx + 1, 8, cz, Dir.POS_Y))
                    addQuadRight(quads, ax1, ay0, ay1, az0, az1, uv.u, uv.v);

                if (!hasFace(shell, cx, 8, cz - 1, Dir.POS_Y))
                    addQuadFront(quads, ax0, ax1, ay0, ay1, az0, uv.u, uv.v);

                if (!hasFace(shell, cx, 8, cz + 1, Dir.POS_Y))
                    addQuadBack(quads, ax0, ax1, ay0, ay1, az1, uv.u, uv.v);
            }
        }
    }

    private static void emitHeadSeamStrips(List<BakedQuad3D> quads,
                                           Map<CellKey, ShellCell> shell,
                                           PartDefinition part) {

        float x0 = part.minX;
        float x1 = part.maxX();
        float y0 = part.minY;
        float y1 = part.maxY();
        float z0 = part.minZ;
        float z1 = part.maxZ();

        float frontMinY = y0 - HEAD_FRONT_UP - HEAD_FRONT_HEIGHT_EXTRA - HEAD_FRONT_VERTICAL_EXTRA;
        float frontMaxY = y1 + HEAD_FRONT_DOWN + HEAD_FRONT_HEIGHT_EXTRA + HEAD_FRONT_VERTICAL_EXTRA;
        float frontPy = (frontMaxY - frontMinY) / 8.0f;

        float outerMinX = x0 - HEAD_SIDE_DEPTH - HEAD_FRONT_WIDTH_EXTRA;
        float outerMaxX = x1 + HEAD_SIDE_DEPTH + HEAD_FRONT_WIDTH_EXTRA;
        float outerMinZ = z0 - HEAD_FRONT_DEPTH;
        float outerMaxZ = z1 + HEAD_BACK_DEPTH;

        float outerPx = (outerMaxX - outerMinX) / 8.0f;
        float outerPz = (outerMaxZ - outerMinZ) / 8.0f;

        // -------------------------------------------------
        // Vertical corner seams
        // -------------------------------------------------

        for (int row = 0; row < 8; row++) {
            float ay0 = frontMinY + row * frontPy;
            float ay1 = ay0 + frontPy;

            // FRONT-LEFT
            if (hasFace(shell, 0, row, -1, Dir.NEG_Z) && hasFace(shell, -1, row, 0, Dir.NEG_X)) {
                boolean topOpen = (row == 0)
                        ? !hasCornerFTL(shell)
                        : !(hasFace(shell, 0, row - 1, -1, Dir.NEG_Z) && hasFace(shell, -1, row - 1, 0, Dir.NEG_X));

                boolean bottomOpen = (row == 7)
                        ? !hasCornerFBL(shell)
                        : !(hasFace(shell, 0, row + 1, -1, Dir.NEG_Z) && hasFace(shell, -1, row + 1, 0, Dir.NEG_X));

                addSeamBoxFrontLeft(quads, ay0, ay1, topOpen, bottomOpen);
            }

            // FRONT-RIGHT
            if (hasFace(shell, 7, row, -1, Dir.NEG_Z) && hasFace(shell, 8, row, 0, Dir.POS_X)) {
                boolean topOpen = (row == 0)
                        ? !hasCornerFTR(shell)
                        : !(hasFace(shell, 7, row - 1, -1, Dir.NEG_Z) && hasFace(shell, 8, row - 1, 0, Dir.POS_X));

                boolean bottomOpen = (row == 7)
                        ? !hasCornerFBR(shell)
                        : !(hasFace(shell, 7, row + 1, -1, Dir.NEG_Z) && hasFace(shell, 8, row + 1, 0, Dir.POS_X));

                addSeamBoxFrontRight(quads, ay0, ay1, topOpen, bottomOpen);
            }

            // BACK-LEFT
            if (hasFace(shell, 0, row, 8, Dir.POS_Z) && hasFace(shell, -1, row, 7, Dir.NEG_X)) {
                boolean topOpen = (row == 0)
                        ? !hasCornerBTL(shell)
                        : !(hasFace(shell, 0, row - 1, 8, Dir.POS_Z) && hasFace(shell, -1, row - 1, 7, Dir.NEG_X));

                boolean bottomOpen = (row == 7)
                        ? !hasCornerBBL(shell)
                        : !(hasFace(shell, 0, row + 1, 8, Dir.POS_Z) && hasFace(shell, -1, row + 1, 7, Dir.NEG_X));

                addSeamBoxBackLeft(quads, ay0, ay1, topOpen, bottomOpen);
            }

            // BACK-RIGHT
            if (hasFace(shell, 7, row, 8, Dir.POS_Z) && hasFace(shell, 8, row, 7, Dir.POS_X)) {
                boolean topOpen = (row == 0)
                        ? !hasCornerBTR(shell)
                        : !(hasFace(shell, 7, row - 1, 8, Dir.POS_Z) && hasFace(shell, 8, row - 1, 7, Dir.POS_X));

                boolean bottomOpen = (row == 7)
                        ? !hasCornerBBR(shell)
                        : !(hasFace(shell, 7, row + 1, 8, Dir.POS_Z) && hasFace(shell, 8, row + 1, 7, Dir.POS_X));

                addSeamBoxBackRight(quads, ay0, ay1, topOpen, bottomOpen);
            }
        }

        // -------------------------------------------------
        // Top / bottom seams running left-right
        // -------------------------------------------------

        for (int i = 0; i < 8; i++) {
            float ax0 = outerMinX + i * outerPx;
            float ax1 = ax0 + outerPx;

            // TOP-FRONT
            if (hasFace(shell, i, -1, 0, Dir.NEG_Y) && hasFace(shell, i, 0, -1, Dir.NEG_Z)) {
                boolean leftOpen = (i == 0)
                        ? !hasCornerFTL(shell)
                        : !(hasFace(shell, i - 1, -1, 0, Dir.NEG_Y) && hasFace(shell, i - 1, 0, -1, Dir.NEG_Z));

                boolean rightOpen = (i == 7)
                        ? !hasCornerFTR(shell)
                        : !(hasFace(shell, i + 1, -1, 0, Dir.NEG_Y) && hasFace(shell, i + 1, 0, -1, Dir.NEG_Z));

                addSeamBoxTopFront(quads, ax0, ax1, leftOpen, rightOpen);
            }

            // TOP-BACK
            if (hasFace(shell, i, -1, 7, Dir.NEG_Y) && hasFace(shell, i, 0, 8, Dir.POS_Z)) {
                boolean leftOpen = (i == 0)
                        ? !hasCornerBTL(shell)
                        : !(hasFace(shell, i - 1, -1, 7, Dir.NEG_Y) && hasFace(shell, i - 1, 0, 8, Dir.POS_Z));

                boolean rightOpen = (i == 7)
                        ? !hasCornerBTR(shell)
                        : !(hasFace(shell, i + 1, -1, 7, Dir.NEG_Y) && hasFace(shell, i + 1, 0, 8, Dir.POS_Z));

                addSeamBoxTopBack(quads, ax0, ax1, leftOpen, rightOpen);
            }

            // BOTTOM-FRONT
            if (hasFace(shell, i, 8, 0, Dir.POS_Y) && hasFace(shell, i, 7, -1, Dir.NEG_Z)) {
                boolean leftOpen = (i == 0)
                        ? !hasCornerFBL(shell)
                        : !(hasFace(shell, i - 1, 8, 0, Dir.POS_Y) && hasFace(shell, i - 1, 7, -1, Dir.NEG_Z));

                boolean rightOpen = (i == 7)
                        ? !hasCornerFBR(shell)
                        : !(hasFace(shell, i + 1, 8, 0, Dir.POS_Y) && hasFace(shell, i + 1, 7, -1, Dir.NEG_Z));

                addSeamBoxBottomFront(quads, ax0, ax1, leftOpen, rightOpen);
            }

            // BOTTOM-BACK
            if (hasFace(shell, i, 8, 7, Dir.POS_Y) && hasFace(shell, i, 7, 8, Dir.POS_Z)) {
                boolean leftOpen = (i == 0)
                        ? !hasCornerBBL(shell)
                        : !(hasFace(shell, i - 1, 8, 7, Dir.POS_Y) && hasFace(shell, i - 1, 7, 8, Dir.POS_Z));

                boolean rightOpen = (i == 7)
                        ? !hasCornerBBR(shell)
                        : !(hasFace(shell, i + 1, 8, 7, Dir.POS_Y) && hasFace(shell, i + 1, 7, 8, Dir.POS_Z));

                addSeamBoxBottomBack(quads, ax0, ax1, leftOpen, rightOpen);
            }
        }

        // -------------------------------------------------
        // Top / bottom seams running front-back
        // -------------------------------------------------

        for (int i = 0; i < 8; i++) {
            float az0 = outerMinZ + i * outerPz;
            float az1 = az0 + outerPz;

            // TOP-LEFT
            if (hasFace(shell, 0, -1, i, Dir.NEG_Y) && hasFace(shell, -1, 0, i, Dir.NEG_X)) {
                boolean frontOpen = (i == 0)
                        ? !hasCornerFTL(shell)
                        : !(hasFace(shell, 0, -1, i - 1, Dir.NEG_Y) && hasFace(shell, -1, 0, i - 1, Dir.NEG_X));

                boolean backOpen = (i == 7)
                        ? !hasCornerBTL(shell)
                        : !(hasFace(shell, 0, -1, i + 1, Dir.NEG_Y) && hasFace(shell, -1, 0, i + 1, Dir.NEG_X));

                addSeamBoxTopLeft(quads, az0, az1, frontOpen, backOpen);
            }

            // TOP-RIGHT
            if (hasFace(shell, 7, -1, i, Dir.NEG_Y) && hasFace(shell, 8, 0, i, Dir.POS_X)) {
                boolean frontOpen = (i == 0)
                        ? !hasCornerFTR(shell)
                        : !(hasFace(shell, 7, -1, i - 1, Dir.NEG_Y) && hasFace(shell, 8, 0, i - 1, Dir.POS_X));

                boolean backOpen = (i == 7)
                        ? !hasCornerBTR(shell)
                        : !(hasFace(shell, 7, -1, i + 1, Dir.NEG_Y) && hasFace(shell, 8, 0, i + 1, Dir.POS_X));

                addSeamBoxTopRight(quads, az0, az1, frontOpen, backOpen);
            }

            // BOTTOM-LEFT
            if (hasFace(shell, 0, 8, i, Dir.POS_Y) && hasFace(shell, -1, 7, i, Dir.NEG_X)) {
                boolean frontOpen = (i == 0)
                        ? !hasCornerFBL(shell)
                        : !(hasFace(shell, 0, 8, i - 1, Dir.POS_Y) && hasFace(shell, -1, 7, i - 1, Dir.NEG_X));

                boolean backOpen = (i == 7)
                        ? !hasCornerBBL(shell)
                        : !(hasFace(shell, 0, 8, i + 1, Dir.POS_Y) && hasFace(shell, -1, 7, i + 1, Dir.NEG_X));

                addSeamBoxBottomLeft(quads, az0, az1, frontOpen, backOpen);
            }

            // BOTTOM-RIGHT
            if (hasFace(shell, 7, 8, i, Dir.POS_Y) && hasFace(shell, 8, 7, i, Dir.POS_X)) {
                boolean frontOpen = (i == 0)
                        ? !hasCornerFBR(shell)
                        : !(hasFace(shell, 7, 8, i - 1, Dir.POS_Y) && hasFace(shell, 8, 7, i - 1, Dir.POS_X));

                boolean backOpen = (i == 7)
                        ? !hasCornerBBR(shell)
                        : !(hasFace(shell, 7, 8, i + 1, Dir.POS_Y) && hasFace(shell, 8, 7, i + 1, Dir.POS_X));

                addSeamBoxBottomRight(quads, az0, az1, frontOpen, backOpen);
            }
        }
    }

    private static void emitHeadCornerBoxes(List<BakedQuad3D> quads,
                                            Map<CellKey, ShellCell> shell,
                                            PartDefinition part) {

        float x0 = part.minX;
        float x1 = part.maxX();
        float y0 = part.minY;
        float y1 = part.maxY();
        float z0 = part.minZ;
        float z1 = part.maxZ();

        // FRONT TOP LEFT
        if (hasCornerFTL(shell)) {
            addCornerFTL(quads, x0, y0, z0);
        }

        // FRONT TOP RIGHT
        if (hasCornerFTR(shell)) {
            addCornerFTR(quads, x1, y0, z0);
        }

        // FRONT BOTTOM LEFT
        if (hasCornerFBL(shell)) {
            addCornerFBL(quads, x0, y1, z0);
        }

        // FRONT BOTTOM RIGHT
        if (hasCornerFBR(shell)) {
            addCornerFBR(quads, x1, y1, z0);
        }

        // BACK TOP LEFT
        if (hasCornerBTL(shell)) {
            addCornerBTL(quads, x0, y0, z1);
        }

        // BACK TOP RIGHT
        if (hasCornerBTR(shell)) {
            addCornerBTR(quads, x1, y0, z1);
        }

        // BACK BOTTOM LEFT
        if (hasCornerBBL(shell)) {
            addCornerBBL(quads, x0, y1, z1);
        }

        // BACK BOTTOM RIGHT
        if (hasCornerBBR(shell)) {
            addCornerBBR(quads, x1, y1, z1);
        }
    }

    private static void addCornerFTL(List<BakedQuad3D> quads, float x0, float y0, float z0) {
        float xMin = x0 - HEAD_SIDE_DEPTH - HEAD_SEAM_EPS;
        float xMax = x0 - HEAD_EPS;
        float yMin = y0 - HEAD_TOP_DEPTH - HEAD_SEAM_EPS;
        float yMax = y0 - HEAD_EPS;
        float zMin = z0 - HEAD_FRONT_DEPTH - HEAD_SEAM_EPS;
        float zMax = z0 - HEAD_EPS;

        addQuadFront(quads, xMin, xMax, yMin, yMax, zMin, 40, 8);
        addQuadLeft(quads, xMin, yMin, yMax, zMin, zMax, 39, 8);
        addQuadTop(quads, xMin, xMax, yMin, zMin, zMax, 40, 0);
    }

    private static void addCornerFTR(List<BakedQuad3D> quads, float x1, float y0, float z0) {
        float xMin = x1 + HEAD_EPS;
        float xMax = x1 + HEAD_SIDE_DEPTH + HEAD_SEAM_EPS;
        float yMin = y0 - HEAD_TOP_DEPTH - HEAD_SEAM_EPS;
        float yMax = y0 - HEAD_EPS;
        float zMin = z0 - HEAD_FRONT_DEPTH - HEAD_SEAM_EPS;
        float zMax = z0 - HEAD_EPS;

        addQuadFront(quads, xMin, xMax, yMin, yMax, zMin, 47, 8);
        addQuadRight(quads, xMax, yMin, yMax, zMin, zMax, 48, 8);
        addQuadTop(quads, xMin, xMax, yMin, zMin, zMax, 47, 0);
    }

    private static void addCornerFBL(List<BakedQuad3D> quads, float x0, float y1, float z0) {
        float xMin = x0 - HEAD_SIDE_DEPTH - HEAD_SEAM_EPS;
        float xMax = x0 - HEAD_EPS;
        float yMin = y1 + HEAD_EPS;
        float yMax = y1 + HEAD_BOTTOM_DEPTH + HEAD_SEAM_EPS;
        float zMin = z0 - HEAD_FRONT_DEPTH - HEAD_SEAM_EPS;
        float zMax = z0 - HEAD_EPS;

        addQuadFront(quads, xMin, xMax, yMin, yMax, zMin, 40, 15);
        addQuadLeft(quads, xMin, yMin, yMax, zMin, zMax, 32, 15);
        addQuadBottom(quads, xMin, xMax, yMax, zMin, zMax, 48, 0);
    }

    private static void addCornerFBR(List<BakedQuad3D> quads, float x1, float y1, float z0) {
        float xMin = x1 + HEAD_EPS;
        float xMax = x1 + HEAD_SIDE_DEPTH + HEAD_SEAM_EPS;
        float yMin = y1 + HEAD_EPS;
        float yMax = y1 + HEAD_BOTTOM_DEPTH + HEAD_SEAM_EPS;
        float zMin = z0 - HEAD_FRONT_DEPTH - HEAD_SEAM_EPS;
        float zMax = z0 - HEAD_EPS;

        addQuadFront(quads, xMin, xMax, yMin, yMax, zMin, 47, 15);
        addQuadRight(quads, xMax, yMin, yMax, zMin, zMax, 48, 15);
        addQuadBottom(quads, xMin, xMax, yMax, zMin, zMax, 55, 0);
    }

    private static void addCornerBTL(List<BakedQuad3D> quads, float x0, float y0, float z1) {
        float xMin = x0 - HEAD_SIDE_DEPTH - HEAD_SEAM_EPS;
        float xMax = x0 - HEAD_EPS;
        float yMin = y0 - HEAD_TOP_DEPTH - HEAD_SEAM_EPS;
        float yMax = y0 - HEAD_EPS;
        float zMin = z1 + HEAD_EPS;
        float zMax = z1 + HEAD_BACK_DEPTH + HEAD_SEAM_EPS;

        addQuadBack(quads, xMin, xMax, yMin, yMax, zMax, 63, 8);
        addQuadLeft(quads, xMin, yMin, yMax, zMin, zMax, 32, 8);
        addQuadTop(quads, xMin, xMax, yMin, zMin, zMax, 40, 0);
    }

    private static void addCornerBTR(List<BakedQuad3D> quads, float x1, float y0, float z1) {
        float xMin = x1 + HEAD_EPS;
        float xMax = x1 + HEAD_SIDE_DEPTH + HEAD_SEAM_EPS;
        float yMin = y0 - HEAD_TOP_DEPTH - HEAD_SEAM_EPS;
        float yMax = y0 - HEAD_EPS;
        float zMin = z1 + HEAD_EPS;
        float zMax = z1 + HEAD_BACK_DEPTH + HEAD_SEAM_EPS;

        addQuadBack(quads, xMin, xMax, yMin, yMax, zMax, 56, 8);
        addQuadRight(quads, xMax, yMin, yMax, zMin, zMax, 55, 8);
        addQuadTop(quads, xMin, xMax, yMin, zMin, zMax, 47, 0);
    }

    private static void addCornerBBL(List<BakedQuad3D> quads, float x0, float y1, float z1) {
        float xMin = x0 - HEAD_SIDE_DEPTH - HEAD_SEAM_EPS;
        float xMax = x0 - HEAD_EPS;
        float yMin = y1 + HEAD_EPS;
        float yMax = y1 + HEAD_BOTTOM_DEPTH + HEAD_SEAM_EPS;
        float zMin = z1 + HEAD_EPS;
        float zMax = z1 + HEAD_BACK_DEPTH + HEAD_SEAM_EPS;

        addQuadBack(quads, xMin, xMax, yMin, yMax, zMax, 63, 15);
        addQuadLeft(quads, xMin, yMin, yMax, zMin, zMax, 32, 15);
        addQuadBottom(quads, xMin, xMax, yMax, zMin, zMax, 48, 7);
    }

    private static void addCornerBBR(List<BakedQuad3D> quads, float x1, float y1, float z1) {
        float xMin = x1 + HEAD_EPS;
        float xMax = x1 + HEAD_SIDE_DEPTH + HEAD_SEAM_EPS;
        float yMin = y1 + HEAD_EPS;
        float yMax = y1 + HEAD_BOTTOM_DEPTH + HEAD_SEAM_EPS;
        float zMin = z1 + HEAD_EPS;
        float zMax = z1 + HEAD_BACK_DEPTH + HEAD_SEAM_EPS;

        addQuadBack(quads, xMin, xMax, yMin, yMax, zMax, 56, 15);
        addQuadRight(quads, xMax, yMin, yMax, zMin, zMax, 48, 15);
        addQuadBottom(quads, xMin, xMax, yMax, zMin, zMax, 55, 7);
    }

    private static void addSeamBoxFrontLeft(List<BakedQuad3D> quads, float y0s, float y1s, boolean topOpen, boolean bottomOpen) {
        float x0s = PartDefinitions.HEAD.minX - HEAD_SIDE_DEPTH - HEAD_SEAM_EPS;
        float x1s = PartDefinitions.HEAD.minX - HEAD_EPS;
        float z0s = PartDefinitions.HEAD.minZ - HEAD_FRONT_DEPTH - HEAD_SEAM_EPS;
        float z1s = PartDefinitions.HEAD.minZ - HEAD_EPS;

        addQuadFront(quads, x0s, x1s, y0s, y1s, z0s, 40, 8);
        addQuadLeft(quads, x0s, y0s, y1s, z0s, z1s, 32 + 7, 8);
        if (topOpen) addQuadTop(quads, x0s, x1s, y0s, z0s, z1s, 40, 0);
        if (bottomOpen) addQuadBottom(quads, x0s, x1s, y1s, z0s, z1s, 48, 0);
    }

    private static void addSeamBoxFrontRight(List<BakedQuad3D> quads, float y0s, float y1s, boolean topOpen, boolean bottomOpen) {
        float x0s = PartDefinitions.HEAD.maxX() + HEAD_EPS;
        float x1s = PartDefinitions.HEAD.maxX() + HEAD_SIDE_DEPTH + HEAD_SEAM_EPS;
        float z0s = PartDefinitions.HEAD.minZ - HEAD_FRONT_DEPTH - HEAD_SEAM_EPS;
        float z1s = PartDefinitions.HEAD.minZ - HEAD_EPS;

        addQuadFront(quads, x0s, x1s, y0s, y1s, z0s, 47, 8);
        addQuadRight(quads, x1s, y0s, y1s, z0s, z1s, 48, 8);
        if (topOpen) addQuadTop(quads, x0s, x1s, y0s, z0s, z1s, 47, 0);
        if (bottomOpen) addQuadBottom(quads, x0s, x1s, y1s, z0s, z1s, 55, 0);
    }

    private static void addSeamBoxBackLeft(List<BakedQuad3D> quads, float y0s, float y1s, boolean topOpen, boolean bottomOpen) {
        float x0s = PartDefinitions.HEAD.minX - HEAD_SIDE_DEPTH - HEAD_SEAM_EPS;
        float x1s = PartDefinitions.HEAD.minX - HEAD_EPS;
        float z0s = PartDefinitions.HEAD.maxZ() + HEAD_EPS;
        float z1s = PartDefinitions.HEAD.maxZ() + HEAD_BACK_DEPTH + HEAD_SEAM_EPS;

        addQuadBack(quads, x0s, x1s, y0s, y1s, z1s, 63, 8);
        addQuadLeft(quads, x0s, y0s, y1s, z0s, z1s, 32, 8);
        if (topOpen) addQuadTop(quads, x0s, x1s, y0s, z0s, z1s, 40, 0);
        if (bottomOpen) addQuadBottom(quads, x0s, x1s, y1s, z0s, z1s, 55, 7);
    }

    private static void addSeamBoxBackRight(List<BakedQuad3D> quads, float y0s, float y1s, boolean topOpen, boolean bottomOpen) {
        float x0s = PartDefinitions.HEAD.maxX() + HEAD_EPS;
        float x1s = PartDefinitions.HEAD.maxX() + HEAD_SIDE_DEPTH + HEAD_SEAM_EPS;
        float z0s = PartDefinitions.HEAD.maxZ() + HEAD_EPS;
        float z1s = PartDefinitions.HEAD.maxZ() + HEAD_BACK_DEPTH + HEAD_SEAM_EPS;

        addQuadBack(quads, x0s, x1s, y0s, y1s, z1s, 56, 8);
        addQuadRight(quads, x1s, y0s, y1s, z0s, z1s, 55, 8);
        if (topOpen) addQuadTop(quads, x0s, x1s, y0s, z0s, z1s, 47, 0);
        if (bottomOpen) addQuadBottom(quads, x0s, x1s, y1s, z0s, z1s, 48, 7);
    }

    private static void addSeamBoxTopFront(List<BakedQuad3D> quads, float x0s, float x1s, boolean leftOpen, boolean rightOpen) {
        float y0s = PartDefinitions.HEAD.minY - HEAD_TOP_DEPTH - HEAD_SEAM_EPS;
        float y1s = PartDefinitions.HEAD.minY - HEAD_EPS;
        float z0s = PartDefinitions.HEAD.minZ - HEAD_FRONT_DEPTH - HEAD_SEAM_EPS;
        float z1s = PartDefinitions.HEAD.minZ - HEAD_EPS;

        addQuadTop(quads, x0s, x1s, y0s, z0s, z1s, 40, 7);
        addQuadFront(quads, x0s, x1s, y0s, y1s, z0s, 40, 8);
        if (leftOpen) addQuadLeft(quads, x0s, y0s, y1s, z0s, z1s, 40, 0);
        if (rightOpen) addQuadRight(quads, x1s, y0s, y1s, z0s, z1s, 47, 0);
    }

    private static void addSeamBoxTopBack(List<BakedQuad3D> quads, float x0s, float x1s, boolean leftOpen, boolean rightOpen) {
        float y0s = PartDefinitions.HEAD.minY - HEAD_TOP_DEPTH - HEAD_SEAM_EPS;
        float y1s = PartDefinitions.HEAD.minY - HEAD_EPS;
        float z0s = PartDefinitions.HEAD.maxZ() + HEAD_EPS;
        float z1s = PartDefinitions.HEAD.maxZ() + HEAD_BACK_DEPTH + HEAD_SEAM_EPS;

        addQuadTop(quads, x0s, x1s, y0s, z0s, z1s, 40, 0);
        addQuadBack(quads, x0s, x1s, y0s, y1s, z1s, 56, 8);
        if (leftOpen) addQuadLeft(quads, x0s, y0s, y1s, z0s, z1s, 32, 8);
        if (rightOpen) addQuadRight(quads, x1s, y0s, y1s, z0s, z1s, 55, 8);
    }

    private static void addSeamBoxBottomFront(List<BakedQuad3D> quads, float x0s, float x1s, boolean leftOpen, boolean rightOpen) {
        float y0s = PartDefinitions.HEAD.maxY() + HEAD_EPS;
        float y1s = PartDefinitions.HEAD.maxY() + HEAD_BOTTOM_DEPTH + HEAD_SEAM_EPS;
        float z0s = PartDefinitions.HEAD.minZ - HEAD_FRONT_DEPTH - HEAD_SEAM_EPS;
        float z1s = PartDefinitions.HEAD.minZ - HEAD_EPS;

        addQuadBottom(quads, x0s, x1s, y1s, z0s, z1s, 48, 0);
        addQuadFront(quads, x0s, x1s, y0s, y1s, z0s, 40, 15);
        if (leftOpen) addQuadLeft(quads, x0s, y0s, y1s, z0s, z1s, 48, 0);
        if (rightOpen) addQuadRight(quads, x1s, y0s, y1s, z0s, z1s, 55, 0);
    }

    private static void addSeamBoxBottomBack(List<BakedQuad3D> quads, float x0s, float x1s, boolean leftOpen, boolean rightOpen) {
        float y0s = PartDefinitions.HEAD.maxY() + HEAD_EPS;
        float y1s = PartDefinitions.HEAD.maxY() + HEAD_BOTTOM_DEPTH + HEAD_SEAM_EPS;
        float z0s = PartDefinitions.HEAD.maxZ() + HEAD_EPS;
        float z1s = PartDefinitions.HEAD.maxZ() + HEAD_BACK_DEPTH + HEAD_SEAM_EPS;

        addQuadBottom(quads, x0s, x1s, y1s, z0s, z1s, 48, 7);
        addQuadBack(quads, x0s, x1s, y0s, y1s, z1s, 56, 15);
        if (leftOpen) addQuadLeft(quads, x0s, y0s, y1s, z0s, z1s, 32, 15);
        if (rightOpen) addQuadRight(quads, x1s, y0s, y1s, z0s, z1s, 48, 15);
    }

    private static void addSeamBoxTopLeft(List<BakedQuad3D> quads, float z0s, float z1s, boolean frontOpen, boolean backOpen) {
        float x0s = PartDefinitions.HEAD.minX - HEAD_SIDE_DEPTH - HEAD_SEAM_EPS;
        float x1s = PartDefinitions.HEAD.minX - HEAD_EPS;
        float y0s = PartDefinitions.HEAD.minY - HEAD_TOP_DEPTH - HEAD_SEAM_EPS;
        float y1s = PartDefinitions.HEAD.minY - HEAD_EPS;

        addQuadTop(quads, x0s, x1s, y0s, z0s, z1s, 40, 0);
        addQuadLeft(quads, x0s, y0s, y1s, z0s, z1s, 32, 8);
        if (frontOpen) addQuadFront(quads, x0s, x1s, y0s, y1s, z0s, 40, 8);
        if (backOpen) addQuadBack(quads, x0s, x1s, y0s, y1s, z1s, 63, 8);
    }

    private static void addSeamBoxTopRight(List<BakedQuad3D> quads, float z0s, float z1s, boolean frontOpen, boolean backOpen) {
        float x0s = PartDefinitions.HEAD.maxX() + HEAD_EPS;
        float x1s = PartDefinitions.HEAD.maxX() + HEAD_SIDE_DEPTH + HEAD_SEAM_EPS;
        float y0s = PartDefinitions.HEAD.minY - HEAD_TOP_DEPTH - HEAD_SEAM_EPS;
        float y1s = PartDefinitions.HEAD.minY - HEAD_EPS;

        addQuadTop(quads, x0s, x1s, y0s, z0s, z1s, 47, 0);
        addQuadRight(quads, x1s, y0s, y1s, z0s, z1s, 48, 8);
        if (frontOpen) addQuadFront(quads, x0s, x1s, y0s, y1s, z0s, 47, 8);
        if (backOpen) addQuadBack(quads, x0s, x1s, y0s, y1s, z1s, 56, 8);
    }

    private static void addSeamBoxBottomLeft(List<BakedQuad3D> quads, float z0s, float z1s, boolean frontOpen, boolean backOpen) {
        float x0s = PartDefinitions.HEAD.minX - HEAD_SIDE_DEPTH - HEAD_SEAM_EPS;
        float x1s = PartDefinitions.HEAD.minX - HEAD_EPS;
        float y0s = PartDefinitions.HEAD.maxY() + HEAD_EPS;
        float y1s = PartDefinitions.HEAD.maxY() + HEAD_BOTTOM_DEPTH + HEAD_SEAM_EPS;

        addQuadBottom(quads, x0s, x1s, y1s, z0s, z1s, 48, 0);
        addQuadLeft(quads, x0s, y0s, y1s, z0s, z1s, 32, 15);
        if (frontOpen) addQuadFront(quads, x0s, x1s, y0s, y1s, z0s, 40, 15);
        if (backOpen) addQuadBack(quads, x0s, x1s, y0s, y1s, z1s, 63, 15);
    }

    private static void addSeamBoxBottomRight(List<BakedQuad3D> quads, float z0s, float z1s, boolean frontOpen, boolean backOpen) {
        float x0s = PartDefinitions.HEAD.maxX() + HEAD_EPS;
        float x1s = PartDefinitions.HEAD.maxX() + HEAD_SIDE_DEPTH + HEAD_SEAM_EPS;
        float y0s = PartDefinitions.HEAD.maxY() + HEAD_EPS;
        float y1s = PartDefinitions.HEAD.maxY() + HEAD_BOTTOM_DEPTH + HEAD_SEAM_EPS;

        addQuadBottom(quads, x0s, x1s, y1s, z0s, z1s, 55, 0);
        addQuadRight(quads, x1s, y0s, y1s, z0s, z1s, 48, 15);
        if (frontOpen) addQuadFront(quads, x0s, x1s, y0s, y1s, z0s, 47, 15);
        if (backOpen) addQuadBack(quads, x0s, x1s, y0s, y1s, z1s, 56, 15);
    }

    private static boolean hasCornerFTL(Map<CellKey, ShellCell> shell) {
        return hasFace(shell, 0, 0, -1, Dir.NEG_Z)
                && hasFace(shell, -1, 0, 0, Dir.NEG_X)
                && hasFace(shell, 0, -1, 0, Dir.NEG_Y);
    }

    private static boolean hasCornerFTR(Map<CellKey, ShellCell> shell) {
        return hasFace(shell, 7, 0, -1, Dir.NEG_Z)
                && hasFace(shell, 8, 0, 0, Dir.POS_X)
                && hasFace(shell, 7, -1, 0, Dir.NEG_Y);
    }

    private static boolean hasCornerFBL(Map<CellKey, ShellCell> shell) {
        return hasFace(shell, 0, 7, -1, Dir.NEG_Z)
                && hasFace(shell, -1, 7, 0, Dir.NEG_X)
                && hasFace(shell, 0, 8, 0, Dir.POS_Y);
    }

    private static boolean hasCornerFBR(Map<CellKey, ShellCell> shell) {
        return hasFace(shell, 7, 7, -1, Dir.NEG_Z)
                && hasFace(shell, 8, 7, 0, Dir.POS_X)
                && hasFace(shell, 7, 8, 0, Dir.POS_Y);
    }

    private static boolean hasCornerBTL(Map<CellKey, ShellCell> shell) {
        return hasFace(shell, 0, 0, 8, Dir.POS_Z)
                && hasFace(shell, -1, 0, 7, Dir.NEG_X)
                && hasFace(shell, 0, -1, 7, Dir.NEG_Y);
    }

    private static boolean hasCornerBTR(Map<CellKey, ShellCell> shell) {
        return hasFace(shell, 7, 0, 8, Dir.POS_Z)
                && hasFace(shell, 8, 0, 7, Dir.POS_X)
                && hasFace(shell, 7, -1, 7, Dir.NEG_Y);
    }

    private static boolean hasCornerBBL(Map<CellKey, ShellCell> shell) {
        return hasFace(shell, 0, 7, 8, Dir.POS_Z)
                && hasFace(shell, -1, 7, 7, Dir.NEG_X)
                && hasFace(shell, 0, 8, 7, Dir.POS_Y);
    }

    private static boolean hasCornerBBR(Map<CellKey, ShellCell> shell) {
        return hasFace(shell, 7, 7, 8, Dir.POS_Z)
                && hasFace(shell, 8, 7, 7, Dir.POS_X)
                && hasFace(shell, 7, 8, 7, Dir.POS_Y);
    }



    // =========================================================
    // ORIGINAL IDEA BAKER FOR BODY / ARMS / LEGS
    // =========================================================

    private static PartMesh bakeCuboid(NativeImage image, PartDefinition def) {
        List<BakedQuad3D> quads = new ArrayList<>();

        float inflate = def.inflate;

        float minX = def.minX - inflate;
        float maxX = def.maxX() + inflate;
        float minY = def.minY - inflate;
        float maxY = def.maxY() + inflate;
        float minZ = def.minZ - inflate;
        float maxZ = def.maxZ() + inflate;

        int u = def.u0;
        int v = def.v0;
        int w = def.width;
        int h = def.height;
        int d = def.depth;

        emitFront(image, quads, def, u + d,         v + d,     w, h, minX, maxX, minY, maxY, minZ);
        emitBack(image, quads,  def, u + d + w + d, v + d,     w, h, minX, maxX, minY, maxY, maxZ);
        emitLeft(image, quads,  def, u,             v + d,     d, h, minX, minY, maxY, minZ, maxZ);
        emitRight(image, quads, def, u + d + w,     v + d,     d, h, maxX, minY, maxY, minZ, maxZ);
        emitTop(image, quads,   def, u + d,         v,         w, d, minX, maxX, minY, minZ, maxZ);
        emitBottom(image, quads,def, u + d + w,     v,         w, d, minX, maxX, maxY, minZ, maxZ);



        return new PartMesh(quads);
    }

    private static void emitFront(
            NativeImage image, List<BakedQuad3D> quads, PartDefinition def,
            int u0, int v0, int w, int h,
            float minX, float maxX, float minY, float maxY, float zShell
    ) {
        float depth = BASE_DEPTH * partScale(def) * faceScaleFrontBack(def);
        float zOuter = zShell - depth;

        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                if (!opaque(image, u0 + px, v0 + py)) continue;

                boolean leftOpen   = px == 0     || !opaque(image, u0 + px - 1, v0 + py);
                boolean rightOpen  = px == w - 1 || !opaque(image, u0 + px + 1, v0 + py);
                boolean topOpen    = py == 0     || !opaque(image, u0 + px, v0 + py - 1);
                boolean bottomOpen = py == h - 1 || !opaque(image, u0 + px, v0 + py + 1);

                float x0 = lerp(px,     0, w, minX, maxX);
                float x1 = lerp(px + 1, 0, w, minX, maxX);
                float y0 = lerp(py,     0, h, minY, maxY);
                float y1 = lerp(py + 1, 0, h, minY, maxY);

                if (py == 0) {
                    y0 -= 0.0005F;
                }

                addQuadFront(quads, x0, x1, y0, y1, zOuter, u0 + px, v0 + py);

                if (leftOpen) {
                    addQuadLeft(quads, x0, y0, y1, zShell, zOuter, u0 + px, v0 + py);
                }
                if (rightOpen) {
                    addQuadRight(quads, x1, y0, y1, zShell, zOuter, u0 + px, v0 + py);
                }
                if (topOpen) {
                    addQuadTop(quads, x0, x1, y0, zShell, zOuter, u0 + px, v0 + py);
                }
                if (bottomOpen) {
                    addQuadBottom(quads, x0, x1, y1, zShell, zOuter, u0 + px, v0 + py);
                }
            }
        }
    }

    private static void emitBack(
            NativeImage image, List<BakedQuad3D> quads, PartDefinition def,
            int u0, int v0, int w, int h,
            float minX, float maxX, float minY, float maxY, float zShell
    ) {
        float depth = BASE_DEPTH * partScale(def) * faceScaleFrontBack(def);
        float zOuter = zShell + depth;

        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                if (!opaque(image, u0 + px, v0 + py)) continue;

                boolean leftOpen   = px == 0     || !opaque(image, u0 + px - 1, v0 + py);
                boolean rightOpen  = px == w - 1 || !opaque(image, u0 + px + 1, v0 + py);
                boolean topOpen    = py == 0     || !opaque(image, u0 + px, v0 + py - 1);
                boolean bottomOpen = py == h - 1 || !opaque(image, u0 + px, v0 + py + 1);

                float x0 = lerp(px,     0, w, maxX, minX);
                float x1 = lerp(px + 1, 0, w, maxX, minX);
                float y0 = lerp(py,     0, h, minY, maxY);
                float y1 = lerp(py + 1, 0, h, minY, maxY);

                if (py == 0) {
                    y0 -= 0.0005F;
                }

                addQuadBack(quads, x0, x1, y0, y1, zOuter, u0 + px, v0 + py);

                if (leftOpen) {
                    addQuadRight(quads, x0, y0, y1, zOuter, zShell, u0 + px, v0 + py);
                }
                if (rightOpen) {
                    addQuadLeft(quads, x1, y0, y1, zOuter, zShell, u0 + px, v0 + py);
                }
                if (topOpen) {
                    addQuadTop(quads, x0, x1, y0, zOuter, zShell, u0 + px, v0 + py);
                }
                if (bottomOpen) {
                    addQuadBottom(quads, x0, x1, y1, zOuter, zShell, u0 + px, v0 + py);
                }
            }
        }
    }

    private static void emitLeft(
            NativeImage image, List<BakedQuad3D> quads, PartDefinition def,
            int u0, int v0, int d, int h,
            float xShell, float minY, float maxY, float minZ, float maxZ
    ) {
        float depth = BASE_DEPTH * partScale(def) * faceScaleSide(def);
        float xOuter = xShell - depth;

        for (int py = 0; py < h; py++) {
            for (int pz = 0; pz < d; pz++) {
                if (!opaque(image, u0 + pz, v0 + py)) continue;

                boolean frontOpen  = pz == 0     || !opaque(image, u0 + pz - 1, v0 + py);
                boolean backOpen   = pz == d - 1 || !opaque(image, u0 + pz + 1, v0 + py);
                boolean topOpen    = py == 0     || !opaque(image, u0 + pz, v0 + py - 1);
                boolean bottomOpen = py == h - 1 || !opaque(image, u0 + pz, v0 + py + 1);

                float z0 = lerp(pz,     0, d, maxZ, minZ);
                float z1 = lerp(pz + 1, 0, d, maxZ, minZ);
                float y0 = lerp(py,     0, h, minY, maxY);
                float y1 = lerp(py + 1, 0, h, minY, maxY);

                addQuadLeftFace(quads, xOuter, y0, y1, z0, z1, u0 + pz, v0 + py);

                if (frontOpen) {
                    addQuadFront(quads, xOuter, xShell, y0, y1, z0, u0 + pz, v0 + py);
                }
                if (backOpen) {
                    addQuadBack(quads, xOuter, xShell, y0, y1, z1, u0 + pz, v0 + py);
                }
                if (topOpen) {
                    addQuadTopX(quads, xOuter, xShell, y0, z0, z1, u0 + pz, v0 + py);
                }
                if (bottomOpen) {
                    addQuadBottomX(quads, xOuter, xShell, y1, z0, z1, u0 + pz, v0 + py);
                }
            }
        }
    }

    private static void emitRight(
            NativeImage image, List<BakedQuad3D> quads, PartDefinition def,
            int u0, int v0, int d, int h,
            float xShell, float minY, float maxY, float minZ, float maxZ
    ) {
        float depth = BASE_DEPTH * partScale(def) * faceScaleSide(def);
        float xOuter = xShell + depth;

        for (int py = 0; py < h; py++) {
            for (int pz = 0; pz < d; pz++) {
                if (!opaque(image, u0 + pz, v0 + py)) continue;

                boolean frontOpen  = pz == 0     || !opaque(image, u0 + pz - 1, v0 + py);
                boolean backOpen   = pz == d - 1 || !opaque(image, u0 + pz + 1, v0 + py);
                boolean topOpen    = py == 0     || !opaque(image, u0 + pz, v0 + py - 1);
                boolean bottomOpen = py == h - 1 || !opaque(image, u0 + pz, v0 + py + 1);

                float z0 = lerp(pz,     0, d, minZ, maxZ);
                float z1 = lerp(pz + 1, 0, d, minZ, maxZ);
                float y0 = lerp(py,     0, h, minY, maxY);
                float y1 = lerp(py + 1, 0, h, minY, maxY);

                addQuadRightFace(quads, xOuter, y0, y1, z0, z1, u0 + pz, v0 + py);

                if (frontOpen) {
                    addQuadFront(quads, xShell, xOuter, y0, y1, z0, u0 + pz, v0 + py);
                }
                if (backOpen) {
                    addQuadBack(quads, xShell, xOuter, y0, y1, z1, u0 + pz, v0 + py);
                }
                if (topOpen) {
                    addQuadTopX(quads, xShell, xOuter, y0, z0, z1, u0 + pz, v0 + py);
                }
                if (bottomOpen) {
                    addQuadBottomX(quads, xShell, xOuter, y1, z0, z1, u0 + pz, v0 + py);
                }
            }
        }
    }

    private static void emitTop(
            NativeImage image, List<BakedQuad3D> quads, PartDefinition def,
            int u0, int v0, int w, int d,
            float minX, float maxX, float yShell, float minZ, float maxZ
    ) {
        float depth = BASE_DEPTH * partScale(def) * faceScaleTopBottom(def) * 1.15F;
        float yOuter = yShell - depth - 0.0005F;

        for (int pz = 0; pz < d; pz++) {
            for (int px = 0; px < w; px++) {
                if (!opaque(image, u0 + px, v0 + pz)) continue;

                boolean leftOpen  = px == 0     || !opaque(image, u0 + px - 1, v0 + pz);
                boolean rightOpen = px == w - 1 || !opaque(image, u0 + px + 1, v0 + pz);
                boolean frontOpen = pz == 0     || !opaque(image, u0 + px, v0 + pz - 1);
                boolean backOpen  = pz == d - 1 || !opaque(image, u0 + px, v0 + pz + 1);

                float x0 = lerp(px,     0, w, minX, maxX);
                float x1 = lerp(px + 1, 0, w, minX, maxX);
                float z0 = lerp(pz,     0, d, maxZ, minZ);
                float z1 = lerp(pz + 1, 0, d, maxZ, minZ);

                addQuadTopFace(quads, x0, x1, yOuter, z0, z1, u0 + px, v0 + pz);

                if (leftOpen) {
                    addQuadLeft(quads, x0, yOuter, yShell, z0, z1, u0 + px, v0 + pz);
                }
                if (rightOpen) {
                    addQuadRight(quads, x1, yOuter, yShell, z0, z1, u0 + px, v0 + pz);
                }
                if (frontOpen) {
                    addQuadFrontZ(quads, x0, x1, yOuter, yShell, z0, u0 + px, v0 + pz);
                }
                if (backOpen) {
                    addQuadBackZ(quads, x0, x1, yOuter, yShell, z1, u0 + px, v0 + pz);
                }
            }
        }
    }

    private static void emitBottom(
            NativeImage image, List<BakedQuad3D> quads, PartDefinition def,
            int u0, int v0, int w, int d,
            float minX, float maxX, float yShell, float minZ, float maxZ
    ) {
        float depth = BASE_DEPTH * partScale(def) * faceScaleTopBottom(def);
        float yOuter = yShell + depth;

        for (int pz = 0; pz < d; pz++) {
            for (int px = 0; px < w; px++) {
                if (!opaque(image, u0 + px, v0 + pz)) continue;

                boolean leftOpen  = px == 0     || !opaque(image, u0 + px - 1, v0 + pz);
                boolean rightOpen = px == w - 1 || !opaque(image, u0 + px + 1, v0 + pz);
                boolean frontOpen = pz == 0     || !opaque(image, u0 + px, v0 + pz - 1);
                boolean backOpen  = pz == d - 1 || !opaque(image, u0 + px, v0 + pz + 1);

                float x0 = lerp(px,     0, w, minX, maxX);
                float x1 = lerp(px + 1, 0, w, minX, maxX);
                float z0 = lerp(pz,     0, d, maxZ, minZ);
                float z1 = lerp(pz + 1, 0, d, maxZ, minZ);

                addQuadBottomFace(quads, x0, x1, yOuter, z0, z1, u0 + px, v0 + pz);

                if (leftOpen) {
                    addQuadLeft(quads, x0, yShell, yOuter, z0, z1, u0 + px, v0 + pz);
                }
                if (rightOpen) {
                    addQuadRight(quads, x1, yShell, yOuter, z0, z1, u0 + px, v0 + pz);
                }
                if (frontOpen) {
                    addQuadFrontZ(quads, x0, x1, yShell, yOuter, z0, u0 + px, v0 + pz);
                }
                if (backOpen) {
                    addQuadBackZ(quads, x0, x1, yShell, yOuter, z1, u0 + px, v0 + pz);
                }
            }
        }
    }

    private static void addQuadFront(List<BakedQuad3D> quads, float x0, float x1, float y0, float y1, float z, int u, int v) {
        quads.add(new BakedQuad3D(
                vtx(x0, y0, z, u, v),
                vtx(x1, y0, z, u + 1, v),
                vtx(x1, y1, z, u + 1, v + 1),
                vtx(x0, y1, z, u, v + 1),
                0.0F, 0.0F, -1.0F
        ));
    }

    private static void addQuadBack(List<BakedQuad3D> quads, float x0, float x1, float y0, float y1, float z, int u, int v) {
        quads.add(new BakedQuad3D(
                vtx(x0, y0, z, u, v),
                vtx(x1, y0, z, u + 1, v),
                vtx(x1, y1, z, u + 1, v + 1),
                vtx(x0, y1, z, u, v + 1),
                0.0F, 0.0F, 1.0F
        ));
    }

    private static void addQuadLeft(List<BakedQuad3D> quads, float x, float y0, float y1, float z0, float z1, int u, int v) {
        quads.add(new BakedQuad3D(
                vtx(x, y0, z0, u, v),
                vtx(x, y0, z1, u + 1, v),
                vtx(x, y1, z1, u + 1, v + 1),
                vtx(x, y1, z0, u, v + 1),
                -1.0F, 0.0F, 0.0F
        ));
    }

    private static void addQuadRight(List<BakedQuad3D> quads, float x, float y0, float y1, float z0, float z1, int u, int v) {
        quads.add(new BakedQuad3D(
                vtx(x, y0, z0, u, v),
                vtx(x, y0, z1, u + 1, v),
                vtx(x, y1, z1, u + 1, v + 1),
                vtx(x, y1, z0, u, v + 1),
                1.0F, 0.0F, 0.0F
        ));
    }

    private static void addQuadTop(List<BakedQuad3D> quads, float x0, float x1, float y, float z0, float z1, int u, int v) {
        quads.add(new BakedQuad3D(
                vtx(x0, y, z0, u, v),
                vtx(x1, y, z0, u + 1, v),
                vtx(x1, y, z1, u + 1, v + 1),
                vtx(x0, y, z1, u, v + 1),
                0.0F, -1.0F, 0.0F
        ));
    }

    private static void addQuadBottom(List<BakedQuad3D> quads, float x0, float x1, float y, float z0, float z1, int u, int v) {
        quads.add(new BakedQuad3D(
                vtx(x0, y, z0, u, v),
                vtx(x1, y, z0, u + 1, v),
                vtx(x1, y, z1, u + 1, v + 1),
                vtx(x0, y, z1, u, v + 1),
                0.0F, 1.0F, 0.0F
        ));
    }

    private static void addQuadLeftFace(List<BakedQuad3D> quads, float x, float y0, float y1, float z0, float z1, int u, int v) {
        addQuadLeft(quads, x, y0, y1, z0, z1, u, v);
    }

    private static void addQuadRightFace(List<BakedQuad3D> quads, float x, float y0, float y1, float z0, float z1, int u, int v) {
        addQuadRight(quads, x, y0, y1, z0, z1, u, v);
    }

    private static void addQuadTopFace(List<BakedQuad3D> quads, float x0, float x1, float y, float z0, float z1, int u, int v) {
        addQuadTop(quads, x0, x1, y, z0, z1, u, v);
    }

    private static void addQuadBottomFace(List<BakedQuad3D> quads, float x0, float x1, float y, float z0, float z1, int u, int v) {
        addQuadBottom(quads, x0, x1, y, z0, z1, u, v);
    }

    private static void addQuadFrontZ(List<BakedQuad3D> quads, float x0, float x1, float y0, float y1, float z, int u, int v) {
        addQuadFront(quads, x0, x1, y0, y1, z, u, v);
    }

    private static void addQuadBackZ(List<BakedQuad3D> quads, float x0, float x1, float y0, float y1, float z, int u, int v) {
        addQuadBack(quads, x0, x1, y0, y1, z, u, v);
    }

    private static void addQuadTopX(List<BakedQuad3D> quads, float x0, float x1, float y, float z0, float z1, int u, int v) {
        addQuadTop(quads, x0, x1, y, z0, z1, u, v);
    }

    private static void addQuadBottomX(List<BakedQuad3D> quads, float x0, float x1, float y, float z0, float z1, int u, int v) {
        addQuadBottom(quads, x0, x1, y, z0, z1, u, v);
    }

    private static Vertex3D vtx(float x, float y, float z, int u, int v) {
        return new Vertex3D(x / 16.0F, y / 16.0F, z / 16.0F, u / 64.0F, v / 64.0F);
    }

    private static boolean opaque(NativeImage image, int x, int y) {
        int argb = image.getPixelRGBA(x, y);
        int alpha = (argb >>> 24) & 0xFF;
        return alpha > 0;
    }

    private static float lerp(float value, float minIn, float maxIn, float minOut, float maxOut) {
        if (maxIn == minIn) {
            return minOut;
        }
        return minOut + (value - minIn) * (maxOut - minOut) / (maxIn - minIn);
    }
}