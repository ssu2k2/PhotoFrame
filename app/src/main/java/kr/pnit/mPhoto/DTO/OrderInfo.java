package kr.pnit.mPhoto.DTO;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by macmini on 14. 12. 1..
 */
public class OrderInfo implements Serializable{
    public Date date;
    public String number;
    public String goods;
    public String orderNumber;

    public OrderInfo()
    {
        this.date = null;
        this.number = "";
        this.goods = "";
        this.orderNumber = "";
    }

    public void setOrderNumber(String orderNumber)
    {
        this.orderNumber = orderNumber;
    }
    public OrderInfo(Date date, String num , String goods, String code) {
        this.date = date;
        this.number = num;
        this.goods = goods;
    }

    public String agent_name;
    public String agent_phone;

    public void setAgentInfo(String name, String phone) {
        this.agent_name = name;
        this.agent_phone = phone;
    }

    public String user_address;
    public String user_name;
    public String user_phone;

    public void setUserInfo(String name, String phone, String addr) {
        this.user_name = name;
        this.user_address = addr;
        this.user_phone = phone;
    }

    public String good_name;
    public int good_number;
    public int good_price;
    public int delevery_price;

    public void setGoodInfo(String name,  int num, int price, int dele_price){
        this.good_name = name;
        this.good_price = price;
        this.good_number = num;
        this.delevery_price = dele_price;
    }
}
