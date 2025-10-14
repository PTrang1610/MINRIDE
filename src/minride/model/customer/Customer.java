package minride.model.customer;


import minride.core.Entity;
import minride.core.Point2D;


public class Customer implements Entity {
private String id;
private String name;
private String district;
private Point2D location;


public Customer(String id, String name, String district, double x, double y) {
this.id = id; this.name = name; this.district = district; this.location = new Point2D(x,y);
}


@Override public String getId() { return id; }
@Override public String getName() { return name; }


public String getDistrict() { return district; }
public Point2D getLocation() { return location; }


public void setName(String name) { this.name = name; }
public void setDistrict(String d) { this.district = d; }
public void setLocation(double x, double y) { this.location = new Point2D(x,y); }


@Override public String toString() {
return String.format("Customer{id='%s', name='%s', district='%s', loc=(%.1f, %.1f)}",
id, name, district, location.x(), location.y());
}
}
