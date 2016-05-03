package com.shuangtixi.stxcontacts.adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuangtixi.stxcontacts.R;
import com.shuangtixi.stxcontacts.db.Contact;
import com.shuangtixi.stxcontacts.util.FirstLetterUtil;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.FormatFlagsConversionMismatchException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

/**
 * Created by Jcr-PC on 2016/3/11.
 */
public class ContactAdapter extends BaseExpandableListAdapter {
    private Context context;  //上下文Context
    private List<String> groups;//存放包含有联系人的ABCD...联系人分组的数组
    private List<String> Allgroups;//存放所有ABCD...联系人分组的数组
    private List<Contact> contacts;//联系人数据
    private int groupLayoutID;//分组项布局
    private int childLayoutID;//联系人项布局
    private LayoutInflater inflater;//
    private static final String s="ABCDEFGHIJKLMNOPQRSTUVWXYZ#"; //分组依据
    private Map<String,List<Contact>> g_Contact_Relation;//联系人与分组对应关系
    private AssetManager assetManager;//


    public ContactAdapter(Context context,List<Contact> contacts,
                          int groupLayoutID,int childLayoutID) {
        this.context = context;
        this.contacts = contacts;
        this.groupLayoutID = groupLayoutID;
        this.childLayoutID = childLayoutID;
        inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

        initGroups();
        initContactsByGroup();
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return g_Contact_Relation.get(groups.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return g_Contact_Relation.get(groups.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View v;
        if (convertView==null){
            v = inflater.inflate(groupLayoutID,parent,false);
        } else {
            v=convertView;
        }

        TextView tv = (TextView)v.findViewById(R.id.txtGroup);
        tv.setText(groups.get(groupPosition));
        return v;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View v;
        if (convertView ==null){
            v = inflater.inflate(childLayoutID,parent,false);
        } else {
            v=convertView;
        }
        bindChildView(v, childPosition, groupPosition);
        return v;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    //联系人分组
    private void initContactsByGroup(){
        if (contacts!=null){
            groups = new ArrayList<>();
            g_Contact_Relation = new HashMap<>();
            List<Contact> cTmp;
            for (String str : Allgroups) {
                cTmp = new ArrayList<>();
                for (Contact c : contacts) {
                    if (FirstLetterUtil.getFirstLetterFromChinese(c.getName()).equals(str)) {
                        cTmp.add(c);
                    }
                    if (str.equals("#") && c.getName().charAt(0)>='0' && c.getName().charAt(0)<='9'){
                        cTmp.add(c);
                    }
                }
                if (cTmp.size()>0){
                    groups.add(str);
                    g_Contact_Relation.put(str, cTmp);
                }
            }
        }
    }

    //初始化分组数据
    private void initGroups(){
        Allgroups = new ArrayList<String>();
        for (int i = 0;i<s.length();++i){
            Allgroups.add((s.charAt(i)+"").toUpperCase());
        }
    }
    private void bindChildView(View v,int childPosition,int groupPosition){
        ImageView img_header = (ImageView)v.findViewById(R.id.img_header);
        TextView txtName = (TextView)v.findViewById(R.id.txtContactName);
        TextView txtTel = (TextView)v.findViewById(R.id.txtContactTel);
        String contactName = g_Contact_Relation.get(groups.get(groupPosition)).get(childPosition).getName();
        String contactTel = g_Contact_Relation.get(groups.get(groupPosition)).get(childPosition).getTel();

        txtName.setText(contactName);
        txtTel.setText(splitTel(contactTel));
        assetManager = context.getAssets();
        try {
            InputStream in = assetManager.open("Circle256px/"+FirstLetterUtil.getFirstLetterFromChinese(contactName)+".png");
            Bitmap bit = BitmapFactory.decodeStream(in);
            img_header.setImageBitmap(bit);
        } catch (IOException e) {
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


    public int getGroupLayoutID() {
        return groupLayoutID;
    }

    public void setGroupLayoutID(int groupLayoutID) {
        this.groupLayoutID = groupLayoutID;
    }

    public int getChildLayoutID() {
        return childLayoutID;
    }

    public void setChildLayoutID(int childLayoutID) {
        this.childLayoutID = childLayoutID;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
        initContactsByGroup();
    }

    public Map<String, List<Contact>> getG_Contact_Relation() {
        return g_Contact_Relation;
    }

    public void setG_Contact_Relation(Map<String, List<Contact>> g_Contact_Relation) {
        this.g_Contact_Relation = g_Contact_Relation;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }
}
