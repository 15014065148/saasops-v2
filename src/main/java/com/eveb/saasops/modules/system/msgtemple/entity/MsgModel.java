package com.eveb.saasops.modules.system.msgtemple.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;



@Setter
@Getter
@ApiModel(value = "MsgModel", description = "信息模板")
@Table(name = "set_basic_msgModel")
public class MsgModel implements Serializable{
private static final long serialVersionUID=1L;
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@ApiModelProperty(value = "id")
private Integer id;

    //编号
    @ApiModelProperty(value = "编号")
    private String name;
    //信息类型
    @ApiModelProperty(value = "信息类型")
    private Integer msgType;
    
    
    //站内信
    @ApiModelProperty(value = "站内信")
    private String inMail;
    //邮件
    @ApiModelProperty(value = "邮件")
    private String email;
    //短信
    @ApiModelProperty(value = "短信")
    private String phoneMail;
    //状态
    @ApiModelProperty(value = "状态")
    private Integer state;
    //创建者
    @ApiModelProperty(value = "创建者")
    private String creater;
    //创建时间
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    @ApiModelProperty(value = "是否是默认站内信")
    private Integer inMailDef ;
    @ApiModelProperty(value = "是否是默认邮件")
    private Integer emailDef ;
    @ApiModelProperty(value = "是否是默认短信")
    private Integer phoneMailDef;
    //最后一次更新时间
    @ApiModelProperty(value = "最后一次更新时间")
    private Date modifyTime;
    @ApiModelProperty(value = "状态 查询条件")
    @Transient
    private List<Integer> states;
    @Transient
    private String msgName;
    @Transient
    private String ids;
    @Transient
    private List<Integer> msgTypes;

    public static Integer getStateByStates(String states){
        if(states.length() ==1){
            return Integer.parseInt(states);
        }
        return null;
    }


    @Override
    public String toString() {
        return "MsgModel{" +
                "id=" + id +'\'' +
                ", name='" + name + '\'' +
                ", msgType=" + msgType +
                ", inMail='" + inMail + '\'' +
                ", email='" + email + '\'' +
                ", phoneMail='" + phoneMail + '\'' +
                ", state=" + state +'\'' +
                ", creater='" + creater + '\'' +
                ", createTime=" + createTime +'\'' +
                ", inMailDef=" + inMailDef +'\'' +
                ", emailDef=" + emailDef +'\'' +
                ", phoneMailDef=" + phoneMailDef +'\'' +
                ", modifyTime=" + modifyTime +'\'' +
                ", states='" + states + '\'' +
                ", msgName='" + msgName + '\'' +
                '}';
    }
}