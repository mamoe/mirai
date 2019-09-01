package net.mamoe.mirai.message;

/**
 * @author Him188moe
 */
public class Face extends Message {
    private final FaceID id;

    public Face(FaceID id) {
        this.id = id;
    }

    public FaceID getId() {
        return id;
    }

    @Override
    public String toString() {
        // TODO: 2019/9/1
        throw new UnsupportedOperationException();
    }
}
