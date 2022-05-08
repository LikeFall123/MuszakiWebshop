package com.example.muszakiwebshop;

public class WebShopItem {
    private String id;
    private String name;
    private String info;
    private String price;
    private float ratedInfo;
    private int image;
    private int cartedCount;

    public WebShopItem() {
    }

    public WebShopItem(String name, String info, String price, float ratedInfo, int image, int cartedCount) {
        this.name = name;
        this.info = info;
        this.price = price;
        this.ratedInfo = ratedInfo;
        this.image = image;
        this.cartedCount = cartedCount;
    }

    public String getName() {
        return name;
    }
    public String getInfo() {
        return info;
    }
    public String getPrice() {
        return price;
    }
    public float getRatedInfo() {
        return ratedInfo;
    }
    public int getImage() {
        return image;
    }
    public int getCartedCount(){return cartedCount;}

    public String _getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setCartedCount(int cartedCount) {
        this.cartedCount = cartedCount;
    }

    @Override
    public String toString() {
        return "WebShopItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", info='" + info + '\'' +
                ", price='" + price + '\'' +
                ", ratedInfo=" + ratedInfo +
                ", image=" + image +
                ", cartedCount=" + cartedCount +
                '}';
    }
}
