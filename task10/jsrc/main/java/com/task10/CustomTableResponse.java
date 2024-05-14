package com.task10;

public class CustomTableResponse {
    private Integer id;
    private Integer number;
    private Integer places;
    private boolean isVip;
    private int minOrder;

    public CustomTableResponse() {
    }

    public CustomTableResponse(Integer id, Integer number, Integer places, boolean isVip, int minOrder) {
        this.id = id;
        this.number = number;
        this.places = places;
        this.isVip = isVip;
        this.minOrder = minOrder;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getPlaces() {
        return places;
    }

    public void setPlaces(Integer places) {
        this.places = places;
    }

    public boolean isVip() {
        return isVip;
    }

    public void setVip(boolean vip) {
        this.isVip = vip;
    }

    public int getMinOrder() {
        return minOrder;
    }

    public void setMinOrder(int minOrder) {
        this.minOrder = minOrder;
    }

    @Override
    public String toString() {
        return "CustomTable{" +
                "id=" + id +
                ", number=" + number +
                ", places=" + places +
                ", isVip=" + isVip +
                ", minOrder=" + minOrder +
                '}';
    }
}
