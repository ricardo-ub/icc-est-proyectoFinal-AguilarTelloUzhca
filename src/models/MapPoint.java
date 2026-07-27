package models;

public class MapPoint {
    private String id;
    private int x;
    private int y;
    public MapPoint(String id, int x, int y) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id de un MapPoint no puede estar vacío.");
        }
        this.id = id;
        setCoordenadas(x, y);
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public int getX() {
        return x;
    }
    public void setX(int x) {
        setCoordenadas(x, this.y);
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        setCoordenadas(this.x, y);
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MapPoint other = (MapPoint) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "MapPoint [id=" + id + ", x=" + x + ", y=" + y + "]";
    }

    private void setCoordenadas(int x, int y) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Las coordenadas de " + id + " no pueden ser negativas.");
        }
        this.x = x;
        this.y = y;
    }
}
