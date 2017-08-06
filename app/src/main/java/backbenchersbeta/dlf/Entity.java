package backbenchersbeta.dlf;

import java.io.Serializable;


public class Entity implements Serializable {
    private int id;
    private int type;
    private int x,y,z;

    public Entity(int id, int type, int x, int y, int z) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return "backbenchersbeta.dlf.Entity{" +
                "id=" + id +
                ", type=" + type +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}
