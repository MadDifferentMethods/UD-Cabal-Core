package UD.CabalCore.DeltaLayer.mesh;

import java.util.List;

public final class PartMesh {
    private final List<BakedQuad3D> quads;

    public PartMesh(List<BakedQuad3D> quads) {
        this.quads = List.copyOf(quads);
    }

    public List<BakedQuad3D> quads() {
        return quads;
    }

    public boolean isEmpty() {
        return quads.isEmpty();
    }
}