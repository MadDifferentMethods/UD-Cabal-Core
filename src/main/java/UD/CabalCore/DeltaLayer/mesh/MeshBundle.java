package UD.CabalCore.DeltaLayer.mesh;

public final class MeshBundle {
    public final PartMesh head;
    public final PartMesh body;
    public final PartMesh leftArm;
    public final PartMesh rightArm;
    public final PartMesh leftLeg;
    public final PartMesh rightLeg;

    public MeshBundle(
            PartMesh head,
            PartMesh body,
            PartMesh leftArm,
            PartMesh rightArm,
            PartMesh leftLeg,
            PartMesh rightLeg
    ) {
        this.head = head;
        this.body = body;
        this.leftArm = leftArm;
        this.rightArm = rightArm;
        this.leftLeg = leftLeg;
        this.rightLeg = rightLeg;
    }
}