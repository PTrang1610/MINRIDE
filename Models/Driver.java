package Models;

import Interface.IEntity;

public class Driver implements IEntity
{
    private String id;
    private String name;
    private double rating;
    private double x;
    private double y;
    public Driver(String id, String name,double rating, double x,double y)
    {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.x = x;
        this.y = y;
    }
    @Override
    public String getId(){
        return id;
    }
    @Override
    public String getName()
    {
        return name;
    }
    public double getRating()
    {
        return rating;
    }
    public double getX()
    {
        return x;
    }
    public double getY()
    {
        return y;
    }
    @Override
    public String toString()
    {
        return String.format("%-5s | %-10s | %-6.1f | (%.1f, %.1f) |", id, name, rating, x, y);
    }
}
