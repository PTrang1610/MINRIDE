package Models;

import Interface.IEntity;

public class Customer implements IEntity {
    private String id;
    private String name;
    private String district;   // quận/huyện
    private double x;          // tọa độ
    private double y;

    public Customer(String id, String name, String district, double x, double y) {
        this.id = id;
        this.name = name;
        this.district = district;
        this.x = x;
        this.y = y;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getName() { return name; }

    public String getDistrict() { return district; }
    public double getX() { return x; }
    public double getY() { return y; }

    public void setName(String name) { this.name = name; }
    public void setDistrict(String district) { this.district = district; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    @Override
    public String toString() {
        String header = String.format("%-5s | %-10s | %-10s | %-10s |", "ID", "Name", "District", "Location");
        String info = String.format("%-5s | %-10s | %-10s | (%.1f, %.1f)", id, name, district, x, y);
        return header + "\n" + info;
    }
}
