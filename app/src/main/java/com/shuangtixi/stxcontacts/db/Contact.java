package com.shuangtixi.stxcontacts.db;

import org.xutils.DbManager;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
import org.xutils.ex.DbException;

import java.io.Serializable;
import java.util.List;

import cn.bmob.v3.BmobObject;

/**
 * Created by Jcr-PC on 2016/3/8.
 */

@Table(name="contact")
public class Contact implements Serializable{

    private int iconId;

    @Column(name = "id",isId = true)
    private int id;

    @Column(name = "name")
    private String name="";

    @Column(name = "tel")
    private String tel="";

    @Column(name = "info")
    private String info="";

    @Column(name = "class")
    private String classNum="";

    @Column(name = "season")
    private String season="";



    public List<Contact> getAllContacts(DbManager db)throws DbException{
        return db.findAll(Contact.class);
    }

    public static void insertContacts(DbManager db,List<Contact> contacts){
        for (int i = 0;i<contacts.size();i++)
        {
            try {
                db.save(contacts.get(i));
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteAllContacts(DbManager db){
        List<Contact> contacts;
        try {
            contacts = db.findAll(Contact.class);
            if (contacts!=null){
                db.delete(contacts);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public static String splitTel(String s){
        String sTmp="";
        if (s.length()>=11){
            for (int i = 0;i<s.length();++i){
                sTmp += s.charAt(i);
                if (i==2 || i==6){
                    sTmp += " ";
                }
            }
        }
        return sTmp;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public String getClassNum() {
        return classNum;
    }

    public void setClassNum(String classNum) {
        this.classNum = classNum;
    }
}

