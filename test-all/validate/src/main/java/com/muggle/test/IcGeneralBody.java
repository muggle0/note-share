package com.muggle.test;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * ic_general_body实体类
 *
 * @author muggle
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@Entity
@Table(name = "ic_general_body")
@DynamicUpdate
public class IcGeneralBody {
    /***/
    @Id
    @Column(name = "ic_general_body_id")
    private String icGeneralBodyId;
    /**
     * 表头主键
     */
    @MyTest
    private String icGeneralHeadId;
    /**
     * 序号
     */
    private Integer rownum;
    /**
     * 条码号
     */
    @MyTest(message = "barcode")
    private String barcode;
    /**
     * 科目字典-销售方式
     */
    @MyTest
    private String saleTypeId;
    /**
     * 科目字典-级别
     */
    private Long gradeId;
    /**
     * 科目字典-款号
     */
    private Long styleId;
    /**
     * 科目字典-材料
     */
    private Long materialId;
    /**
     * 科目字典-品名
     */
    private Long kindId;
    /**
     * 标签名
     */
    private String labelName;
    /**
     * 证书号
     */
    private String certificate;
    /**
     * 手寸
     */
    private String ringSize;
    /**
     * 件数
     */
    private Double quantity;
    /**
     * 总重
     */
    private Double grossWeight;
    /**
     * 材料重
     */
    private Double matWgt;
    /**
     * 损耗
     */
    private Double lsPct;
    /**
     * 含耗重
     */
    private Double lsWgt;
    /**
     * 折足重
     */
    private Double discWgt;
    /**
     * 材料价格
     */
    private Double matPrc;
    /**
     * 喷拉沙
     */
    private Double sprayFe;
    /**
     * 微镶费
     */
    private Double mrInFe;
    /**
     * 风险费
     */
    private Double riskFee;
    /**
     * 分色分件
     */
    private Double clrPcFe;
    /**
     * 辘珠边费
     */
    private Double rolEdFe;
    /**
     * cnc费
     */
    private Double cncFee;
    /**
     * 封底费
     */
    @MyTest(message = "btmfee")
    private String btmFee;
    /**
     * 成本金价
     */
    private Double costMaterialPrice;
    /**
     * 销售金价
     */
    private Double saleMaterialPrice;
    /**
     * 补口单价
     */
    private Double extraPrice;
    /**
     * 销售金料额
     */
    private Double matAct;
    /**
     * 补口费
     */
    private Double grossExtraPrice;
    /**
     * 证书价费
     */
    private Double cetPre;
    /**
     * 加工费
     */
    private Double prsPre;
    /**
     * 主石数量
     */
    private Double mnStQty;
    /**
     * 主石重量
     */
    private Double mnStWt;
    /**
     * 主石金额
     */
    private Double mnStAmt;
    /**
     * 主石镶工费
     */
    private Double mnStInAmt;
    /**
     * 辅石数量
     */
    private Double lesStQty;
    /**
     * 辅石重
     */
    private Double lesStWt;
    /**
     * 辅石金额
     */
    private Double lesStAmt;
    /**
     * 辅石镶工费
     */
    private Double lesStInAmt;
    /**
     * 起版费
     */
    private Double price;
    /**
     * 其它费用
     */
    private Double otPre;
    /**
     * 标签价格
     */
    private Double labelPrice;
    /**
     * 结算金额
     */
    private Double saleAmount;
    /**
     * 成本金额
     */
    private Double costAmount;
    /**
     * 税率
     */
    private Double taxRate;
    /**
     * 含税金额
     */
    private Double taxAmount;
    /**
     * 销售成本
     */
    private Double saleCost;
    /**
     * 倍率
     */
    private Double rate;
    /**
     * 折扣
     */
    private Double discount;
    /**
     * 配件数
     */
    private Double fittingsQuantity;
    /**
     * 配件重
     */
    private Double fittingsWeight;
    /**
     * 配件金额
     */
    private Double fittingsAmount;
    /**
     * 石头属性表ID
     */
    private Long stoneId;
    /**
     * 石号
     */
    private String stoneCode;
    /**
     * 科目字典-石头名称
     */
    private Long stoneNameId;
    /**
     * 数单价
     */
    private Double numPrice;
    /**
     * 重单价
     */
    private Double weightPrice;
    /**
     * 金额
     */
    @MyTest(message = "account")
    private String account;
    /***/
    private Double sellWage;
    /**
     * 工费方式
     */
    private Long workingTypeId;
    /**
     * 石头规格
     */
    private String stoneSize;
    /**
     * 粒数
     */
    private Integer stonePcs;
    /**
     * 石重
     */
    private Double stoneWeight;
    /**
     * 退回数
     */
    private Integer stoneNum;
    /**
     * 科目字典-形状
     */
    private Long shapeId;
    /**
     * 科目字典-颜色
     */
    private Long colorId;
    /**
     * 科目字典-净度
     */
    private Long clarityId;
    /**
     * 科目字典-切工
     */
    private Long cutId;
    /**
     * 盘点数
     */
    private Integer checkNum;
    /**
     * 库存数
     */
    private Integer stockNum;
    /**
     * 库存重
     */
    private Double stockWeight;
    /**
     * 盘点重
     */
    private Double checkWeight;
    /**
     * 熔金重量
     */
    private Double meltWeight;
    /**
     * 实测成色%
     */
    private Double realClarity;
    /**
     * 公司款号
     */
    private String companyStyleId;
    /**
     * 流水号
     */
    private Integer serialNo;
    /**
     * 入库足重
     */
    private Double inStockPureWeight;
    /**
     * 足金
     */
    private Double pureGoldWeight;
    /**
     * 成色耗率%
     */
    private Double clarityLossPct;
    /**
     * 客账重
     */
    private Double customerStoneWeight;
    /**
     * 客账数
     */
    private Integer customerStoneNum;
    /**
     * 客户石号
     */
    private String customerStoneCode;
    /**
     * 石头退回重
     */
    private Double stoneBackWeight;
    /**
     * 客账足重
     */
    private Double customerPureWeight;
    /**
     * 款式编号
     */
    private String styleCode;
    /**
     * 备注
     */
    private String remark;
    /***/
    private Boolean dr;
    /***/
    private Long corpId;
    /**
     * 成本金料额
     */
    private Double costMatAct;
    /**
     * 旧条码
     */
    private String oldBarcode;
    /**
     * 加工商条码
     */
    private String supplierBarcode;
    /**
     * 礼品费
     */
    private Double giftWage;
    /**
     * 维修费
     */
    private Double repaireFee;
    /**
     * 实际售价
     */
    @MyTest(message = "actuallyCost")
    private String actuallyCost;
    /***/
    private Double policyFee;
    /**
     * 采购主石金额
     */
    private Double buyMnStAmt;
    /**
     * 采购辅石金额
     */
    private Double buyLesStAmt;
    /**
     * 实际成本
     */
    private Double realCost;
    /**
     * 采购数单价
     */
    private Double buyNumPrice;
    /**
     * 采购重单价
     */
    private Double buyWeightPrice;
    /**
     * 采购金额
     */
    private Double buyStoneAmount;
    /**
     * 折足率
     */
    private Double discRate;
    /**
     * 现货重
     */
    private Double nowGrossWeight;
    /**
     * 货重差
     */
    private Double grossWeightGap;


}
