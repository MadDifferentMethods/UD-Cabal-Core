package UD.CabalCore.DeltaLayer.mesh;

public final class BakedQuad3D {
    public final Vertex3D v0;
    public final Vertex3D v1;
    public final Vertex3D v2;
    public final Vertex3D v3;

    public final float nx;
    public final float ny;
    public final float nz;

    public BakedQuad3D(
            Vertex3D v0,
            Vertex3D v1,
            Vertex3D v2,
            Vertex3D v3,
            float nx,
            float ny,
            float nz
    ) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
    }
}