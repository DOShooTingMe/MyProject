package com.yonyou.cpu.vmi.service.supplyReplenishServiceImpl;

import com.alibaba.fastjson.JSONObject;
import com.yonyou.cpu.commons.domain.response.ServiceResponse;
import com.yonyou.cpu.domain.criteria.PurOrderCriteria;
import com.yonyou.cpu.domain.criteria.SaleOrderCriteria;
import com.yonyou.cpu.domain.criteria.SaleOrderDetailCriteria;
import com.yonyou.cpu.domain.dto.Material;
import com.yonyou.cpu.domain.entity.account.MgrUser;
import com.yonyou.cpu.domain.enums.OrderBusiTypeEnum;
import com.yonyou.cpu.domain.order.PurOrder;
import com.yonyou.cpu.domain.order.PurOrderDetail;
import com.yonyou.cpu.domain.order.PurOrderPayTerm;
import com.yonyou.cpu.domain.purorderenum.PayTermChangeEnum;
import com.yonyou.cpu.domain.purorderenum.PurOrderSourceTypeEnum;
import com.yonyou.cpu.domain.purorderenum.PurOrderSplitEnum;
import com.yonyou.cpu.domain.saleorder.SaleOrder;
import com.yonyou.cpu.domain.saleorder.SaleOrderBuinessTypeEnum;
import com.yonyou.cpu.domain.saleorder.SaleOrderDetail;
import com.yonyou.cpu.domain.saleorder.SaleOrderPayTerm;
import com.yonyou.cpu.domain.saleorder.SaleOrderSplitEnum;
import com.yonyou.cpu.material.service.IMaterialService;
import com.yonyou.cpu.pubapp.pattern.data.VOUtils;
import com.yonyou.cpu.pubapp.pattern.scheme.BillQueryScheme;
import com.yonyou.cpu.pubapp.pattern.vo.VODelete;
import com.yonyou.cpu.pubapp.pattern.vo.VOInsert;
import com.yonyou.cpu.pubapp.pattern.vo.VOUpdate;
import com.yonyou.cpu.purorder.api.IPurOrderService;
import com.yonyou.cpu.purorder.api.busi.IPuOrderBusiService;
import com.yonyou.cpu.saleorder.api.ISaleOrderService;
import com.yonyou.cpu.vmi.api.supplyReplenishService.SupplyReplenishService;
import com.yonyou.cpu.vmi.api.vmiboard.IVmiBoardService;
import com.yonyou.cpu.vmi.api.vmiconfig.IVmiConfigService;
import com.yonyou.cpu.vmi.dao.board.IBoardDao;
import com.yonyou.cpu.vmi.domain.criteria.BoardCriteria;
import com.yonyou.cpu.vmi.domain.criteria.PurOrderContractCriteria;
import com.yonyou.cpu.vmi.domain.criteria.PurOrderContractCriteriaDetail;
import com.yonyou.cpu.vmi.domain.replenishmentboard.Board;
import com.yonyou.cpu.vmi.domain.replenishmentboard.BoardEntity;
import com.yonyou.cpu.vmi.domain.replenishmentconfig.Config;
import com.yonyou.cpu.vmistock.IVmiReqSaleOrderNumService;
import com.yonyou.ucf.mdd.common.dto.BaseReqDto;
import com.yonyou.ucf.mdd.common.model.Pager;
import com.yonyou.yuncai.contract.api.IAgreement2OrderService;
import com.yonyou.yuncai.contract.api.IContractMaterialService;
import com.yonyou.yuncai.contract.api.IContractService;
import com.yonyou.yuncai.contract.criteria.ContractCriteria;
import com.yonyou.yuncai.contract.criteria.ContractMaterialCtriteria;
import com.yonyou.yuncai.contract.domain.Contract;
import com.yonyou.yuncai.contract.domain.ContractMaterial;
import com.yonyou.yuncai.contract.domain.ContractPayTerm;
import com.yonyou.yuncai.contract.enums.EControlType;
import com.yonyou.yuncai.contract.enums.EExecType;
import com.yonyou.yuncai.contract.util.ExceptionUtils;
import com.yonyou.yuncai.cpu.basedoc.api.billtype.ITransTypeVOService;
import com.yonyou.yuncai.cpu.basedoc.api.param.IParamService;
import com.yonyou.yuncai.cpu.basedoc.api.person.IPersonService;
import com.yonyou.yuncai.cpu.basedoc.api.supplydocument.ISupplyDocService;
import com.yonyou.yuncai.cpu.domain.criteria.ParamCriteria;
import com.yonyou.yuncai.cpu.domain.dto.billtype.TransTypeVO;
import com.yonyou.yuncai.cpu.domain.dto.param.ParamPOJO;
import com.yonyou.yuncai.cpu.domain.dto.supplydocument.LinkmanPOJO;
import com.yonyou.yuncai.cpu.domain.dto.supplydocument.SupplyDocPOJO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.mysql.fabric.xmlrpc.base.Array;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.cpu.common.bpm.utils.BillStatusEnum;
import org.cpu.common.bpm.utils.BillTypeEnum;
import org.cpu.common.utils.fastjson.JsonFastUtil;
import org.cpu.common.utils.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SupplyReplenishServiceImpl implements SupplyReplenishService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private VOInsert voInsert;// 单表操作

	@Autowired
	private VOUpdate voUpdate;

	@Autowired
	private VODelete voDelete;

	@Autowired
	private BillQueryScheme scheme;

	@Autowired
	private IPersonService iPersonService;// 人员

	@Autowired
	private IPurOrderService purOrderService;// 采购订单操作接口

	@Autowired
	private ISaleOrderService saleOrderService;// 销售订单

	@Autowired
	private IMaterialService iMaterialService;// 物料getMaterialByID

	@Autowired
	private ISupplyDocService iSupplyDocService;// 供应商

	@Autowired
	private IVmiConfigService iVmiConfigService;// vmi配置

	@Autowired
	private ITransTypeVOService iTransTypeVOService;// 交易类型接口

	@Autowired
	private IAgreement2OrderService iAgreement2OrderService;// 合同接口

	@Autowired
	private IContractService contractService;// 合同接口

	@Autowired
	private IContractMaterialService contractMaterialService;// 合同接口

	@Autowired
	private IVmiReqSaleOrderNumService iVmiReqSaleOrderNumService;//

	@Autowired
	private IBoardDao boardDao;

	@Autowired
	private IPuOrderBusiService service;

	@Autowired
	private IParamService paramService;
	
	@Autowired
	private IVmiBoardService vmiBoardService;
	
	@Override
	public Pager queryPage(BaseReqDto<String, String> param) {
		Pager page = scheme.queryPage(param, Board.class);
		return page;
	}

	/**
	 * 补货申请，检查并组织跳转到补货申请单界面的数据
	 */
	@Override
	public JSONObject borderTurnRepCheck(List<BoardEntity> list) {

		JSONObject result = new JSONObject();

		String message = "下述物料不可超量申请补货：物料";

		try {
			if(list==null || list.size()==0 || list.get(0)==null){
				result.put("data", null);
				result.put("msg", "请勾选补货申请数据");
				result.put("status", 0);
				return result;
			}
			Long enterpriseId = list.get(0).getEnterpriseId();
			// 当前列表传递的供应商都是一个
			Long supDocId = list.get(0).getSupDocId();
			if (enterpriseId == null) {
				result.put("status", 0);
				result.put("msg", "json对象的租户id为空，无法判断选取何种校校验方式");
				return result;
			}
			Config[] configs = iVmiConfigService.queryByEnterpriseId(enterpriseId);
			if (configs == null || configs.length==0 || configs[0]==null){
				result.put("status", 0);
				result.put("msg", enterpriseId + "在配置表中对应的config对象为空");
				return result;
			}
			// 0：补货看板->补货申请->补货发货;1：补货看板->补货发货;2：VMI订单->补货发货
			String flowType = configs[0].getFlowType();
			boolean flag = true;
			
			for (int i = 0; i < list.size(); i++) {
				BoardEntity boardEntity = list.get(i);
				if (boardEntity == null){
					result.put("status", 0);
					result.put("msg", "前台传递对象不可用");
					return result;
				}
				
				// 业务逻辑：1.根据配置选择不同的校验----
				BigDecimal tempNum = new BigDecimal("0");
				if ("0".equals(flowType)) {
					// 此校验原则：将复选框选中的行需要做本次申请补货量<=本期可申请补货剩余量量的校验
					// 本次申请补货量
					BigDecimal thisAskfillNum = boardEntity.getThisAskfillNum();// 50
					// 本期可申请补货剩余量
					BigDecimal currentAskSurplusNum = boardEntity.getCurrentAskSurplusNum();// 40
					
					if (thisAskfillNum != null && currentAskSurplusNum != null) {
						if(thisAskfillNum.compareTo(tempNum) <= 0){
							message = "数量输入不合法，请校验后填入";
							result.put("status", 0);
							result.put("msg", message);
							return result;
						}
						// 物料名称
						String materialName = boardEntity.getMaterialName();
						if (thisAskfillNum.compareTo(currentAskSurplusNum) > 0) {
							// 不正常
							message += materialName + ",";
							flag = false;
						}
					} else {
						message = "本次申请补货量或者本期可申请补货剩余量为空";
						result.put("status", 0);
						result.put("msg", message);
						return result;
					}
				} else if ("1".equals(flowType)) {
					// 此校验原则：将复选框选中的行需要做本次补货量<=本期可补货剩余量
					// 本次补货量
					BigDecimal thisfillNum = boardEntity.getThisfillNum();// 50
					// 本期可补货剩余量
					BigDecimal currentSurplusNum = boardEntity.getCurrentSurplusNum();// 40
					if (thisfillNum != null && currentSurplusNum != null) {
						//校验数量不能为负数
						if(thisfillNum.compareTo(tempNum) < 0){
							message = "本次补货量为负数，请校验后填入";
							result.put("status", 0);
							result.put("msg", message);
							return result;
						}
						// 物料名称
						String materialName = boardEntity.getMaterialName();
						if (thisfillNum.compareTo(currentSurplusNum) > 0) {
							// 不正常
							message += materialName + ",";
							flag = false;
						}
					} else {
						message = "本次补货量或者本期可补货剩余量为空";
						result.put("status", 0);
						result.put("msg", message);
						return result;
					}
				} else if ("2".equals(flowType)) {
					result.put("msg", "接口分支留出、后期补充");
					return result;
				} else {
					result.put("status", 0);
					result.put("msg", enterpriseId + "在配置表中对应的flowType 为：" + flowType + ";无此流程");
					return result;
				}
			}
			
			message += "请检查本次补货申请量不可超过可补货申请量";
			if(!flag){
				result.put("status", 0);
				result.put("msg", message);
				result.put("data", null);
				return result;
			}

			// 没有物料发生溢出
			// 获取供应商
			//REMARK:(~.~~.~~.~~.~下面还有一处。要改一起改~.~~.~~.~~.~~.~搜索"if ("Y".equals(isDefault))")
			Long[] ids = { supDocId };
			
			List<SupplyDocPOJO> sypplyPOJOList = iSupplyDocService.getSupplyDocByIds(ids);
			// 我方电话
			String myPhoneNum = "";
			// 我方联系人
			String myUserName = "";
			if (sypplyPOJOList.size() > 0) {
				SupplyDocPOJO sypplyPOJO = sypplyPOJOList.get(0);
				List<LinkmanPOJO> listForLinkmanPOJO = sypplyPOJO.getLinkmanPOJOList();
				if (listForLinkmanPOJO != null) {
					for (int i = 0; i < listForLinkmanPOJO.size(); i++) {
						LinkmanPOJO one = listForLinkmanPOJO.get(i);
						String isDefault = one.getIsDefault();
						// 采购商下面供应商档案有个默认联系人
						if ("Y".equals(isDefault)) {
							myPhoneNum = (one.getCell() == null?one.getPhone():one.getCell());
							myUserName = one.getName();
							break;
						}
					}
				}
			}
			
			// 数据填充
			for (int q = 0; q < list.size(); q++) {
				BoardEntity one = list.get(q);
				one.setMyPhoneNum(myPhoneNum);
				one.setMyUserName(myUserName);
			}
			
			// 返回
			String data = JsonFastUtil.toJson(list);
			
			result.put("status", 1);
			result.put("data", data);
			result.put("msg", "校验通过");
		} catch (Exception e) {
			e.printStackTrace();
			result.put("data", null);
			result.put("msg", e.getMessage());
			result.put("status", 0);
		}
		return result;
	}

	/**
	 * 补货发货生成销售订单服务
	 */
	@Override
	public JSONObject borderTurnRep(String jsonStr) {
		// 处理请求参数
		JSONObject jsonObject = JSONObject.parseObject(jsonStr);

		// 明细内容
		List<BoardEntity> boardEntityList = JsonUtils.fromJsonArray(jsonObject.getString("listInfos"),
				BoardEntity.class);
		// 我方电话
		String myPhoneNum = jsonObject.get("myPhoneNum") == null ? null : jsonObject.get("myPhoneNum") + "";
		// 我方联系人
		String myUserName = jsonObject.get("myUserName") == null ? null : (String) jsonObject.get("myUserName");
		// 我方备注
		String myRemark = jsonObject.get("myRemark") == null ? null : (String) jsonObject.get("myRemark");

		JSONObject result = new JSONObject();

		if (myPhoneNum == null || "".equals(myPhoneNum.trim())) {
			result.put("status", 0);
			result.put("msg", "我方联系电话为空");
			return result;
		}

		if (myUserName == null || "".equals(myUserName.trim())) {
			result.put("status", 0);
			result.put("msg", "我方联系人为空");
			return result;
		}

		if (boardEntityList == null || boardEntityList.size() == 0) {
			result.put("status", 0);
			result.put("msg", "物料选择为空，请选择物料！");
			return result;
		}

		Long enterpriseId = boardEntityList.get(0).getEnterpriseId();
		if (enterpriseId == null) {
			result.put("status", 0);
			result.put("msg", "json对象的租户id为空，无法判断选取何种校校验方式");
			return result;
		}

		Config[] configs = iVmiConfigService.queryByEnterpriseId(enterpriseId);
		if (configs == null || configs.length == 0) {
			result.put("status", 0);
			result.put("msg", enterpriseId + "在配置表中对应的config对象为空");
			return result;
		}

		String flowType = configs[0].getFlowType();
		if (!"0".equals(flowType)) {
			result.put("status", 0);
			result.put("msg", "VMI配置流程不是补货看板-->补货申请-->补货发货，请检查！");
			return result;
		}

		// 返回结果
		String message = "下述物料不可超量申请补货：物料";
		// 不超额标志
		boolean numNotOverFlag = true;

		// 将数据处理、转存数据库
		//得到物料ID，订单上需要补充物料分类id，得到物料id查询下物料。
		List<Long> materialIdList = new ArrayList<Long>();
		for (BoardEntity board : boardEntityList) {
			materialIdList.add(board.getMaterialId());
			// 此校验原则：将复选框选中的行需要做本次申请补货量<=本期可申请补货剩余量量的校验
			// 本次申请补货量
			BigDecimal thisAskfillNum = board.getThisAskfillNum();// 50
			// 本期可申请补货剩余量
			// TODO:校验数量不能直接取看板上的量，需要计算得到
			BigDecimal currentAskSurplusNum = board.getCurrentAskSurplusNum();// 40
			
			//校验用
			BigDecimal tempNum = new BigDecimal("0");
			
			// TODO：需要在之前校验输入的发货申请数量是否为空；而不是做非空判断
			if (thisAskfillNum != null && currentAskSurplusNum != null) {
				//校验数量
				if(thisAskfillNum.compareTo(tempNum) <= 0){
					message = "数量输入不合法，请校验后填入";
					result.put("status", 0);
					result.put("msg", message);
					return result;
				}
				
				if (thisAskfillNum.compareTo(currentAskSurplusNum) > 0) {
					// 物料名称
					String materialName = board.getMaterialName();
					// 不正常
					message += materialName + ",";
					numNotOverFlag = false;
				}
			}
			
			//计划到货时间：
			Date planDeliverDate = board.getPlanDeliverDate() ;
			if (planDeliverDate != null && !"".equals(planDeliverDate)) {
				Date date = new Date();
				try {
					if(planDeliverDate.getTime()<date.getTime()){
						result.put("status", 0);
						result.put("msg", "计划到货时间必须大于当前时间");
						return result;
					}
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
				}
			}
		}

		// 不超额
		if (!numNotOverFlag) {
			message += "请检查本次补货申请量不可超过可补货申请量";
			result.put("stats", 0);
			result.put("msg", message);
			return result;
		}

		//根据物料id查询物料
		Material[] materials = iMaterialService.getMaterialByIDs(materialIdList.toArray(new Long[0]));
		Map<Long,Material> id2MaterialMap = new HashMap<Long,Material>();
		if(materials!=null && materials.length>0){
			for(Material material : materials){
				id2MaterialMap.put(material.getId(),material);
			}
		}
		// 订单生成：
		// 订单规则：客户+采购组织+采购员
		Map<String, List<BoardEntity>> orgAndPurPerson2BoardListMap = splitOrder(boardEntityList);
		for (String key : orgAndPurPerson2BoardListMap.keySet()) {
			List<BoardEntity> boardList = orgAndPurPerson2BoardListMap.get(key);

			// 订单子表集合
			List<SaleOrderDetail> listDetail = new ArrayList<SaleOrderDetail>();
			int lineNum = 1;
			for (BoardEntity board : boardList) {
				// 子表
				SaleOrderDetail saleOrderDetail = new SaleOrderDetail();
				// 产品名称
				saleOrderDetail.setProductName(board.getMaterialName());
				// 行号
				saleOrderDetail.setLineNum(lineNum++);

				// 申请数量 前端传给我的；
				// saleDetail中的数量是根据配置灵活赋值的
				saleOrderDetail.setQuantity(board.getThisAskfillNum());
				// 单位
				saleOrderDetail.setUnit(board.getUnitName());
				// 创建时间
				saleOrderDetail.setGmtCreate(new Date());
				// 货品描述(生成销售订单先空着)？？？
				// 缺少物品的材质 和产地
				Long materialId = board.getMaterialId();
				if (materialId != null) {
					saleOrderDetail.setProductIid(String.valueOf(materialId));
					//补充物料分类
					Material material = id2MaterialMap.get(materialId);
					if(material!=null){
						saleOrderDetail.setMaterialclassId(material.getClassId());
						saleOrderDetail.setMaterialclassName(material.getClassName());
					}
				}
				// 物料：
				// 外系统过来的物料/产品ID
				// 物料规格
				saleOrderDetail.setProductSpec(board.getSpec());
				// 物料型号
				saleOrderDetail.setProductModel(board.getModel());
				// 物料code
				saleOrderDetail.setMaterialCode(board.getMaterialCode());

				// 仓库code
				saleOrderDetail.setRecvstorCode(board.getWhCode());
				// 收货组织code
				saleOrderDetail.setRecOrgCode(board.getReceiverCode());
				// 租户id
				saleOrderDetail.setEnterpriseId(board.getSupEnterpriseId());

				// 收货组织id = board中的库存组织`store_org_id`
				saleOrderDetail.setRecv_org(board.getStoreOrgId());
				// 收货组织name = board中的库存组织``
				saleOrderDetail.setRecv_orgname(board.getStoreOrgName());
				// 收货组织code = board中的库存组织``
				saleOrderDetail.setRecOrgCode(board.getStoreOrgCode());
				// 计划到货日期
				saleOrderDetail.setPlanDeliverDate(board.getPlanDeliverDate());
				// 备注
				// logger.error("前端传递的核实单中明细的备注字段："+board.getRemark());
				saleOrderDetail.setMemo(board.getRemark());

				// 收货人id
				saleOrderDetail.setReceivePersonId(board.getReceiverId());
				// 收货人name
				saleOrderDetail.setRecPeople(board.getReceiverName());
				// 收货地址
				saleOrderDetail.setDeliverAddress(board.getReceiveAddress());
				// 收货人电话
				saleOrderDetail.setContactWay(board.getReceiverMobile());

				saleOrderDetail.setDr(0);
				listDetail.add(saleOrderDetail);
			}

			BoardEntity board = boardList.get(0);
			// 订单主表对象
			SaleOrder order = new SaleOrder();
			// 订单子表赋值
			order.setDetailEntityList(listDetail);
			// 订单状态：待发布
			order.setOrderStatus(SaleOrderSplitEnum.TOBERELEASED.getCode());
			// 创建时间
			order.setGmtCreate(new Date());

			// 收货人邮编
			// order.setToPost(personPOJO.getpos);
			// 收货人地址
			order.setToArea(listDetail.get(0).getDeliverAddress());
			// 供应商id
			order.setEnterpriseId(board.getSupEnterpriseId());
			// 供应商name
			order.setEnterpriseName(board.getSupEnterpriseName());

			// 采购商租户id
			order.setPurEnterpriseId(board.getEnterpriseId());
			// 采购商租户name
			order.setPurEnterpriseName(board.getEnterpriseName());

			// 时间戳
			order.setTs(new Date());
			// 单据状态先写0后期再说？？？
			order.setBillstatus(0);
			// 订单日期(生成销售订单先空着。后期生成采购订单的时候回写过来)？？？
			order.setOrderTime(new Date());
			// 采购员
			order.setPurchaseId(board.getPurPersonUserId());
			// 采购员姓名
			order.setPurchaseName(board.getPurPersonName());
			// 采购员电话
			order.setPurchasePhone(board.getPurPersonMobile());
			order.setPurPersonId(board.getPurPersonId());
			order.setPurPersonName(board.getPurPersonName());

			// 采购组织id
			order.setOrgId(Long.valueOf(board.getPurOrgId()));
			// 采购组织name
			order.setOrgName(board.getPurOrgName());

			// 基础默认值
			order.setPriceType("1");
			order.setIndustryTag(1);
			order.setBusinessType(Integer.parseInt(SaleOrderBuinessTypeEnum.SPECIALIZED.getCode()));
			order.setUrgency(1);

			// 订单来源
			order.setOrderSource(PurOrderSourceTypeEnum.ORDERSOURCE.getCode());

			// 供应商联系人
			// 获取供应商
			// board表中字段：供应商id。用来存在订单的主表
			Long supDocId = null;
			// 供应商档案id
			supDocId = board.getSupDocId();
			//
			Long[] ids = { supDocId };
			List<SupplyDocPOJO> sypplyPOJOList = iSupplyDocService.getSupplyDocByIds(ids);
			if (sypplyPOJOList.size() > 0) {

				SupplyDocPOJO sypplyPOJO = sypplyPOJOList.get(0);

				List<LinkmanPOJO> listForLinkmanPOJO = sypplyPOJO.getLinkmanPOJOList();

				if (listForLinkmanPOJO != null) {
					for (int q = 0; q < listForLinkmanPOJO.size(); q++) {
						LinkmanPOJO linkManPojo = listForLinkmanPOJO.get(q);
						String isDefault = linkManPojo.getIsDefault();
						// 采购商下面供应商档案有个默认联系人
						if ("Y".equals(isDefault)) {
							myPhoneNum = (linkManPojo.getCell() == null?linkManPojo.getPhone():linkManPojo.getCell());
							myUserName = linkManPojo.getName();
							order.setSupplyPersionId(linkManPojo.getLinkmanId());
							// 供应商联系人name
							order.setSupplyPersionName(myUserName);
							break;
						}
					}
				}
			}
			// 供应商id
			order.setSupplierid(("".equals(supDocId) ? null : Long.valueOf(supDocId)));
			// 供应商name
			order.setSupplierName(myUserName);
			// 供应商电话
			order.setSupplyPhone(myPhoneNum);
			// 人命比
			order.setCurrencyName("人民币");
			order.setCurrencyCode("人民币");
			// 业务类型
			String businessType = SaleOrderBuinessTypeEnum.SPECIALIZED.getCode();
			order.setBusinessType(Integer.valueOf(businessType));

			// 交易类型查询：businesstype：1：普通；2：VIM；approve：1：是；0：否；
			TransTypeVO transTypeVO = getTransactionInfo(businessType, order.getPurEnterpriseId());
			if (transTypeVO != null) {
				// 交易类型id
				order.setTransactionTypeId(transTypeVO.getPkTranstrype());
				// 交易类型code
				order.setTransactionTypeCode(transTypeVO.getVtranstrypeCode());
				order.setTransactionTypeName(transTypeVO.getVtranstrypeName());
			}
			// 方便测试，后续打开
			order.setSubject("VMI补货订单");
			// 删除
			order.setDr(0);
			// 备注
			order.setMemo(myRemark);
			// 开票类型(暂时定1)
			order.setInvoice_state(1);
			// order.setOrderno("VMI测试销售订单");

			// order_flow_type:补货订单申请生效流程:(放到销售订单生成的地方，从枚举中取值，直接存储)
			// 0：YC补货申请订单->买方确认申请订单->买方YC审批
			// 1：YC补货申请订单->买方确认申请订单->买方YC审批->ERP审批态订单；
			// 2：YC补货申请订单->买方确认申请订单->ERP自由态订单->ERP订单审批；
			// 3：YC补货申请订单->买方确认申请订单->ERP审批态订单；
			String orderFlowType = configs[0].getOrderFlowType();
			if (orderFlowType != null && !"".equals(orderFlowType)) {
				// 获取 “补货订单申请生效流程”
				if ("0".equals(orderFlowType)) {
					order.setBusitype(OrderBusiTypeEnum.VMIAPPLY_PURYCCONFIRM_PURYCAPPROVE.getCode());
				} else if ("1".equals(orderFlowType)) {
					order.setBusitype(OrderBusiTypeEnum.VMIAPPLY_PURYCCONFIRM_PURYCAPPROVE_ERORDERAPPROVE.getCode());
				} else if ("2".equals(orderFlowType)) {
					order.setBusitype(OrderBusiTypeEnum.VMIAPPLY_PURYCCONFIRM_ERPORDERSAVE_ERAPPROVE.getCode());
				} else {
					order.setBusitype(OrderBusiTypeEnum.VMIAPPLY_PURYCCONFIRM_ERPORDERAPPROVE.getCode());
				}
			}

			ServiceResponse<Integer> flagInsert = saleOrderService.saveSaleOrderForVMI(order);// insert

			result.put("status", 1);
			result.put("msg", "保存成功");
		}
		return result;
	}

	/**
	 * @Title:2.
	 * @param:@param saleOrderCriteria
	 * @param:@return
	 * @return:
	 * @Description: 通过请求参数类 SaleOrderCriteria 获取 SaleOrder对象和他的子表
	 * @author:liuhao7@yonyou.com
	 * @date:2019年10月12日
	 */
	private SaleOrder getSaleOrderObj(SaleOrderCriteria saleOrderCriteria) {
		if (saleOrderCriteria == null || saleOrderCriteria.getId() == null) {
			return null;
		}
		// 得到含有子表的主表信息通过id查询--这里查到的销售订单，已经包含了表体信息
		ServiceResponse<String> respSOList = saleOrderService.getSaleOrderList(saleOrderCriteria);
		String soListJson = (String) respSOList.getResult();
		List<SaleOrder> soList = soListJson == null ? null : JsonUtils.fromJsonArray(soListJson, SaleOrder.class);
		SaleOrder saleOrder = null;
		if (soList != null && soList.size() > 0) {
			saleOrder = soList.get(0);
		}

		return saleOrder;
	}

	/**
	 * @Title:3.
	 * @param:@param purOrderCriteria
	 * @param:@param flag:是否要表体
	 * @param:@return
	 * @return:
	 * @Description:通过请求对象获取后端采购订单对象,
	 * @author:liuhao7@yonyou.com
	 * @date:2019年10月12日
	 */
	private PurOrder getPurOrder(PurOrderCriteria purOrderCriteria) {
		if (purOrderCriteria == null || purOrderCriteria.getId() == null) {
			return null;
		}

		ServiceResponse<String> serviceResponse = purOrderService.getPurOrderList(purOrderCriteria);

		if (serviceResponse == null) {
			return null;
		}

		// 得到字符串
		String strjson = serviceResponse.getResult();

		// 得到sale对象
		JSONArray jSONArray = JSONArray.parseArray(strjson);

		PurOrder purOrder = jSONArray.getObject(0, PurOrder.class);

		return purOrder;
	}

	/**
	 * 补货申请单分单
	 * 
	 * @param boardList
	 * @return
	 */
	private Map<String, List<BoardEntity>> splitOrder(List<BoardEntity> boardList) {
		Map<String, List<BoardEntity>> orgAndPurPerson2BoardListMap = new HashMap<String, List<BoardEntity>>();

		for (BoardEntity board : boardList) {//zhaowenfei
			String key = "org" + board.getPurOrgId() + "person" + board.getPurPersonId();
			if (!orgAndPurPerson2BoardListMap.containsKey(key)) {
				List<BoardEntity> boards = new ArrayList<BoardEntity>();
				orgAndPurPerson2BoardListMap.put(key, boards);
			}
			orgAndPurPerson2BoardListMap.get(key).add(board);
		}

		return orgAndPurPerson2BoardListMap;
	}

	/**
	 * @Title:6.
	 * @param:@return
	 * @return:
	 * @Description: 得到属于VMI交易类型的对象
	 * @author:liuhao7@yonyou.com
	 * @date:2019年10月17日
	 */
	private TransTypeVO getTransactionInfo(String busType, Long enterpriseId) {
		// ycDemo 上存储的业务类型为 1：普通；2：VMI；
		try {
			List<TransTypeVO> listTrans = iTransTypeVOService.selectByBustypeAndApprove(busType, enterpriseId);
			TransTypeVO transTypeVO = new TransTypeVO();
			if (listTrans.size() > 0) {
				for (TransTypeVO one : listTrans) {
					if ("1".equals(one.getApprove())) {
						return one;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * @Title:7.
	 * @param:@param saleOrder
	 * @param:@return
	 * @return:
	 * @Description: 根据销售订单主表得到采购订单的主表
	 * @author:liuhao7@yonyou.com
	 * @date:2019年10月16日
	 */
	private PurOrder getPurorderBySaleOrder(SaleOrder saleOrder) {

		// 前台没有给传递。自己取一下。
		SaleOrderCriteria saleOrderCriteria = new SaleOrderCriteria();
		saleOrderCriteria.setId(saleOrder.getId());
		// TODO：为什么要查一次销售订单？
		SaleOrder saleOrderDB = getSaleOrderObj(saleOrderCriteria);

		PurOrder purOrder = new PurOrder();

		// 订单状态
		purOrder.setOrderStatus(PurOrderSplitEnum.TOBECONFIRMEDBYTHEBUYER.getCode());

		// 创建时间
		purOrder.setGmtCreate(new Date());
		// 收货人邮编
		purOrder.setToPost(saleOrder.getToPost());
		// 收货人地址
		purOrder.setToArea(saleOrder.getToArea());

		// 企业号
		purOrder.setEnterpriseId(saleOrder.getPurEnterpriseId());
		// 企业name
		purOrder.setEnterpriseName(saleOrder.getPurEnterpriseName());

		// 供应商的租户id
		// 供应商的租户名称
		purOrder.setSupEnterpriseId(saleOrder.getEnterpriseId());
		purOrder.setSupEnterpriseName(saleOrder.getEnterpriseName());

		// 单据状态
		Integer billstatus = 0;
		purOrder.setBillstatus(billstatus);

		// 订单标题？？？？？？
		String subject = "VMI补货订单";
		purOrder.setSubject(subject);

		// 供应商ID
		Long supplierid = saleOrder.getSupplierid();
		if (supplierid == null) {
			// 自己取
			purOrder.setSupplierid(saleOrderDB.getSupplierid());
		}
		purOrder.setSupplierName(saleOrder.getSupplierName());

		// 供应商联系人id
		purOrder.setSupplyPersionId(saleOrder.getSupplyPersionId());
		// 供应商联系人Name
		purOrder.setSupplyPersionName(saleOrder.getSupplyPersionName());
		// 供应商联系人
		purOrder.setSupplyPhone(saleOrder.getSupplyPhone());

		// 订单日期??????
		purOrder.setOrderTime(new Date());

		purOrder.setBusinessType(saleOrderDB.getBusinessType());

		// 币种
		purOrder.setCurrencyCode(saleOrderDB.getCurrencyCode());
		purOrder.setCurrencyName(saleOrderDB.getCurrencyCode());

		// 采购员id????
		Long purchaseId = saleOrder.getPurchaseId();
		if (purchaseId == null) {
			// 自己取
			purOrder.setPurchaseId(saleOrderDB.getPurchaseId());
		}

		// 采购员姓名????
		purOrder.setPurchaseName(saleOrder.getPurchaseName());

		// 采购员电话
		purOrder.setPurchasePhone(saleOrder.getPurchasePhone());

		// 供应商电话
		purOrder.setSupplyPhone(saleOrder.getSupplyPhone());

		// 供应商联系人
		purOrder.setSupplyPersionId(saleOrder.getSupplyPersionId());

		// 供应商联系人名称
		purOrder.setSupplyPersionName(saleOrder.getSupplyPersionName());

		// 订单来源标识 云采 1688
		// 先写这个后期？？？
		// purOrder.setOrderSource();
		// 订单来源
		purOrder.setSourceType(PurOrderSourceTypeEnum.ORDERSOURCE.getCode());

		// 响应时间
		// purOrder.setTresptime(new Date());

		// 采购组织id
		purOrder.setOrgId(saleOrderDB.getOrgId());
		purOrder.setOrgName(saleOrderDB.getOrgName());

		// 存放业务流程
		String busitype = saleOrderDB.getBusitype();
		/*
		 * 0：YC补货申请订单->买方确认申请订单->买方YC审批 1：YC补货申请订单->买方确认申请订单->买方YC审批->ERP审批态订单；
		 * 2：YC补货申请订单->买方确认申请订单->ERP自由态订单->ERP订单审批；
		 * 3：YC补货申请订单->买方确认申请订单->ERP审批态订单；
		 */
		purOrder.setBusitype(busitype);

		// 备注
		String memo = "VMI由销售订单转采购订单";
		purOrder.setDr(0);
		purOrder.setMemo(memo);

		// 交易类型取值
		purOrder.setTransactionTypeCode(saleOrderDB.getTransactionTypeCode());
		purOrder.setTransactionTypeId(saleOrderDB.getTransactionTypeId());
		purOrder.setTransactionTypeName(saleOrderDB.getTransactionTypeName());
		return purOrder;
	}

	/**
	 * @Title:8.
	 * @param:@param saleOrder
	 * @param:@return
	 * @return:
	 * @Description: 根据销售订单子表得到采购订单子表
	 * @author:liuhao7@yonyou.com
	 * @date:2019年10月16日
	 */
	private PurOrderDetail getPurorderDetailBySaleOrder(SaleOrder saleOrder ,SaleOrderDetail saleOrderDetailOne) {

		PurOrderDetail purOrderOne = new PurOrderDetail();
		//补充物料分类，需要使用数据库中查询到的信息，先这么写吧，后面再重构
		List<SaleOrderDetail> detailList = saleOrder.getDetailEntityList();
		Map<Long,SaleOrderDetail> id2detailMap = new HashMap<Long, SaleOrderDetail>();
		for(SaleOrderDetail detail : detailList){
			id2detailMap.put(detail.getId(), detail);
		}
		SaleOrderDetail detailDB = id2detailMap.get(saleOrderDetailOne.getId());
		// 赋值
		// 合同号（协议直采）
		Long contract_id = null;
		// 订单明细ID(来源于外系统)
		String orderDetailId = saleOrderDetailOne.getId().toString();
		purOrderOne.setOrderDetailId(orderDetailId);
		// 供应商商品id
		purOrderOne.setProductIid(saleOrderDetailOne.getProductIid());
		// 名称
		purOrderOne.setProductName(saleOrderDetailOne.getProductName());
		// 型号
		purOrderOne.setProductModel(saleOrderDetailOne.getProductModel());
		// 规格
		purOrderOne.setProductSpec(saleOrderDetailOne.getProductSpec());
		// 编码
		purOrderOne.setMaterialCode(saleOrderDetailOne.getMaterialCode());
		//补充上物料分类信息
        purOrderOne.setMaterialclassId(detailDB==null ? null : detailDB.getMaterialclassId());
        purOrderOne.setMaterialclassName(detailDB==null ? null : detailDB.getMaterialclassName());
        purOrderOne.setMaterialclassCode(detailDB==null ? null : detailDB.getMaterialclassCode());
		// 数量
		purOrderOne.setQuantity(saleOrderDetailOne.getQuantity());
		// 单位
		purOrderOne.setUnit(saleOrderDetailOne.getUnit());
		// 修改时间
		purOrderOne.setGmtCreate(new Date());
		// 单位id
		purOrderOne.setUnitid(saleOrderDetailOne.getUnitid());
		// 计划收货时间？？？？？？？？？？？？？？？？
		purOrderOne.setPlanDeliverDate(saleOrderDetailOne.getPlanDeliverDate());
		// 供应商商品id
		purOrderOne.setSuppProductId(saleOrderDetailOne.getSuppProductId());
		// 供应商商品名称
		purOrderOne.setSuppProductName(saleOrderDetailOne.getSuppProductName());
		
		if(saleOrder!=null && saleOrder.getDetailEntityList()!=null && saleOrder.getDetailEntityList().size()>0){
			for(SaleOrderDetail saleOrderDetail:saleOrder.getDetailEntityList()){
				if(saleOrderDetail.getId().longValue() == saleOrderDetailOne.getId().longValue() ){
					//收货组织：我们存储了收货组织id(recv_org);收货组织code(recOrgCode);收货组织name(recv_orgname)在销售订单中；
					
					// 收货组织编码
					purOrderOne.setRecOrgCode(saleOrderDetail.getRecOrgCode());
					// 收货组织name
					purOrderOne.setReceiveOrgName(saleOrderDetail.getRecv_orgname());
					// 收货组织id
					purOrderOne.setReceiveOrgId(saleOrderDetail.getRecv_org());
					
					//收货仓库：我们存储了收货仓库code(recvstorCode)收货仓库name(recvstor 但是这个没有取值存入)
					
					// 收货仓库code
					purOrderOne.setRecvstorCode(saleOrderDetail.getRecvstorCode());
					// 收货仓库name
					purOrderOne.setRecvstor(saleOrderDetail.getRecvstor());
					break;
				}
			}
		}
		// 备注
		purOrderOne.setMemo(saleOrderDetailOne.getMemo());

		return purOrderOne;
	}

	/**
	 * VMI补货销售订单发布生成采购订单
	 */
	@Override
	public JSONObject saveSaleOrderV(SaleOrder saleOrder, MgrUser user) {// 销售订单保存、发布
		// 业务逻辑：1.保存 ;2.发布：发布生成采购订单同时回写数据到之前的销售订单中；
		JSONObject result = new JSONObject();

		//-1.校验参数
		//提交方式：保存、保存并发布
		//FIXME：没有提供单独的发布服务，即使是非编辑态的发布，也会调用保存并发布服务，并不合适
		String submit = saleOrder.getSubmit();
		if (submit == null || "".equals(submit.trim())) {
			result.put("msg", "销售订单保存时提交方式未传递");
			result.put("status", 0);
			return result;
		}
		// 明细内容;必须传，后期要修改明细数量；
		List<SaleOrderDetail> listDetails = saleOrder.getDetailEntityList();
		if (listDetails == null || listDetails.size() == 0) {
			result.put("msg", "销售订单保存时json串解析数据集合为空");
			result.put("status", 0);
			return result;
		}
		
		// 返回结果和生成核实单的时候不一样样
		String message = "下述物料行已经超量申请：";
		// 提示“第X行，物料A已超量申请，最大可申请补货量=本期可申请补货剩余量1；
		// 第X行，物料B已超量申请，最大可申请补货量=本期可申请补货剩余量2。请检查修正后重新保存或提交”
		// 不超额标志
		boolean flag = true;
		try {
			// 业务逻辑:
			// 0.校验；(根据销售订单id查询销售订单（主子表）,有些数据前端传递不全)；
			SaleOrderCriteria saleOrderCriteria = new SaleOrderCriteria();
			saleOrderCriteria.setId(saleOrder.getId());
			SaleOrder saleOrderFromDb = getSaleOrderObj(saleOrderCriteria);
			
			// 规则：
			// a) 按照客户+物料+供应商+库存组织+仓库维度找到唯一一条VMI看板补货数据，取出补货上限、在途量
			// b) 重新计算对应的“本期已申请通过量”
			// c) 重新计算对应的“本期已申请待买方确认数量”
			// d) 重新计算对应的“本期已申请待卖方确认数量”

			// 最终得到“本期可申请补货剩余量”公式
			// 重新计算除开本张订单外，本期可申请补货剩余量=补货上限-在途量-本期已申请通过量-本期已申请待买方确认数量-本期已申请待卖方确认数量+本订单旧数量
			//REMARK:(~.~~.~~.~~.~下面还有一处。要改一起改~.~~.~~.~~.~~.~搜索"getCurrentAskSurplusNum(此方法已经重构在order里面了，不用管了)")
			
			Map map=new HashMap<>()	;
			
			//设置“本期可申请补货剩余量”的查询条件
            List<String> mdlist=new ArrayList<String>();
            List<String> materialList=new ArrayList<String>();
            List<Long> orgList=new ArrayList<Long>();
            
            //增加粒度判断：库存点粒度0 收货组织+仓库 1 收货组织
            Config[] configs = iVmiConfigService.queryByEnterpriseId(saleOrderFromDb.getPurEnterpriseId());
    		String stockdimension = configs[0].getStockdimension();
    		
    		//查询条件封装key
    		Map mapQuery = new HashMap();
            
        	//查询条件组装
        	map.put("purenterpriseid", saleOrderFromDb.getPurEnterpriseId());
        	map.put("supenterpriseid", saleOrderFromDb.getEnterpriseId());
        	for(int q = 0; q < saleOrderFromDb.getDetailEntityList().size(); q++){
        		//旧订单数据（实体用的saleOrderFromDb 防止前端传递参数不全）
        		SaleOrderDetail saleOrderDetail = saleOrderFromDb.getDetailEntityList().get(q);
        		//物料id
            	materialList.add(saleOrderDetail.getMaterialCode());
            	if(!"".equals(stockdimension) && stockdimension!=null){
        			if("0".equals(stockdimension)){
        				//仓库
        				mdlist.add(saleOrderDetail.getRecvstorCode());
        			}
        		}
        		//组织
            	orgList.add(saleOrderDetail.getRecv_org());
        	}
        	map.put("mdcodeList", mdlist);
        	map.put("materialList",materialList);
        	map.put("orgList", orgList);
        	
        	BoardCriteria boardCriteria  =new BoardCriteria();
        	boardCriteria.setExtFields(map);
        	
        	//查询board;
        	List<BoardEntity> listOfBoards =  vmiBoardService.getThisAskfillNum(boardCriteria);
        	//调用HK接口查询最大可补货量
        	List<BoardEntity> boardOne =vmiBoardService.querypurbyEnterPriseId(configs[0],listOfBoards) ;
        	
        	if(boardOne!=null && boardOne.size()>0){
        		for(BoardEntity board:boardOne){
        			if(!"".equals(stockdimension) && stockdimension!=null){
            			if("0".equals(stockdimension)){
            				//判断粒度是否存在仓库
            				mapQuery.put(board.getEnterpriseId()+board.getSupEnterpriseId()+board.getMaterialCode()+board.getWhCode()+board.getStoreOrgId(), board);
            			}else{
            				mapQuery.put(board.getEnterpriseId()+board.getSupEnterpriseId()+board.getMaterialCode()+board.getStoreOrgId(), board);
            			}
            		}
        			
            	}
        	}
        	
        	String strKey  = "";
        	//订单表体：id+表体；用来和前端传递的订单表体做比较
        	Map<Long,SaleOrderDetail> id2SaleOrderMap=new HashMap<Long,SaleOrderDetail>();
			for (SaleOrderDetail saleOrderDetail : saleOrderFromDb.getDetailEntityList()) {
				if (!"".equals(stockdimension) && stockdimension != null) {
					if ("0".equals(stockdimension)) {
						// 判断粒度是否存在仓库saleOrderDetail
						strKey = saleOrder.getPurEnterpriseId() + saleOrder.getEnterpriseId()
								+ saleOrderDetail.getMaterialCode() + saleOrderDetail.getRecvstorCode()
								+ saleOrderDetail.getRecv_org();
					} else {
						strKey = saleOrder.getPurEnterpriseId() + saleOrder.getEnterpriseId()
								+ saleOrderDetail.getMaterialCode() + saleOrderDetail.getRecv_org();
					}
				}

				if (mapQuery.containsKey(strKey)) {
					// 取出来 "本期可申请补货剩余量" 做判断
					BigDecimal currentAskSurplusNum = ((BoardEntity) mapQuery.get(strKey)).getCurrentAskSurplusNum();
					if (currentAskSurplusNum != null) {
						saleOrderDetail
								.setCurrentAskSurplusNum(currentAskSurplusNum.add(saleOrderDetail.getQuantity()));
					}
					id2SaleOrderMap.put(saleOrderDetail.getId(), saleOrderDetail);
				}
			}
			
			//用来做判断
        	for (int q = 0; q < listDetails.size(); q++) {
        		SaleOrderDetail saleOrderTemp = listDetails.get(q);
        		//校验用
				BigDecimal tempNum = new BigDecimal("0");
        		if(saleOrderTemp.getQuantity().compareTo(tempNum) <= 0){
					message = "本次申请数量为负数，请校验后填入";
					result.put("status", 0);
					result.put("msg", message);
					return result;
				}
        		
        		// 计划到货时间校验
				Date d = saleOrderTemp.getConfirmArriveDate();
				if (d == null) {
					result.put("status", 0);
					result.put("msg", "确认到货时间不能为空");
					return result;
				}
        		
        		if(id2SaleOrderMap.containsKey(saleOrderTemp.getId())){
        			//取出来最大补货量和当前数值做比较
        			BigDecimal decimalApplyRepquantity = ((SaleOrderDetail)id2SaleOrderMap.get(saleOrderTemp.getId())).getCurrentAskSurplusNum();
					BigDecimal decimalQuantity = saleOrderTemp.getQuantity();
        			
        			if (decimalApplyRepquantity.compareTo(decimalQuantity) < 0) {
						// 不正常
						if (q == listDetails.size() - 1) {
							message = message + "第" + (q + 1) + "行,物料" + saleOrderTemp.getProductName() + "已超量申请," + "最大可申请补货量=本期可申请补货剩余量"
									+ decimalApplyRepquantity + "。";
						} else {
							message = message + "第" + (q + 1) + "行,物料" + saleOrderTemp.getProductName() + "已超量申请," + "最大可申请补货量=本期可申请补货剩余量"
									+ decimalApplyRepquantity + ";";
						}
						flag = false;
					}
        			
        		}
        	}
        	
			if (flag) {
				// 每个明细均没有超出限额
				if ("insert".equals(submit)) {
					// 1.更新：子表的备注和数量
					ServiceResponse s = saleOrderService.updateSaleOrderForVMI(saleOrder);// update：采购信息回写
					// 2.反馈
					result.put("data", s.getResult());
					result.put("status", s.getCode());
					result.put("msg", s.getMsg());
				} else if ("publish".equals(submit)) {
					// 业务0.发布：状态修改为: RELEASEERPBILLCOUNT("2", "待买方确认"),
					//增加重复提交业务逻辑判断：
					SaleOrderCriteria saleOrderCriteriaTemp = new SaleOrderCriteria();
					saleOrderCriteriaTemp.setId(saleOrder.getId());
					SaleOrder saleOrderTemp = getSaleOrderObj(saleOrderCriteria);
					if(saleOrderTemp !=null && SaleOrderSplitEnum.RELEASEERPBILLCOUNT.getCode().equals(saleOrderTemp.getOrderStatus())){
						result.put("status", 0);
						result.put("msg", "请勿重复操作");
						return result;
					}
					String orderStatuss = SaleOrderSplitEnum.RELEASEERPBILLCOUNT.getCode();
					saleOrder.setOrderStatus(orderStatuss);

					// 业务1.生成采购单(主表)保存采购单
					PurOrder purOrder = this.getPurorderBySaleOrder(saleOrder);

					// 业务2. 前端是否确认单价结果保存：是否允许确认单价 0-不允许 1-允许
					ParamCriteria criteria = new ParamCriteria();
					criteria.setEnterpriseId(purOrder.getEnterpriseId());
					criteria.setParamDefCode("ORDER005");
					logger.info("查询参数 ：" + JsonUtils.toJson(criteria));
					List<ParamPOJO> listPar = paramService.queryParam(criteria);
					logger.info("查询是否允许确认单价结果 ：{}" + JsonUtils.toJson(listPar));
					if (listPar == null || listPar.size() == 0) {
						purOrder.setIsConPrice("0");
					} else {
						ParamPOJO param = listPar.get(0);
						purOrder.setIsConPrice(param.getValue());
					}
					
					Boolean ifAutoConfirm = true;

					// 业务3.采购订单主表保存（这里为了在sale_order_detail中回写pur_order_detail的id）
					purOrder = purOrderService.savePurOrderForVMI(purOrder);// insert方法；

					// 业务4.销售主表字段回写
					saleOrder.setOrderOtherId(purOrder.getOrderno());// 订单编号(来源于其他系统)
					saleOrder.setPurOrderCode(purOrder.getOrderno());// 采购订单编码
					saleOrder.setPurOrderId(purOrder.getId());// 来源采购订单id
					String order_other_id = (purOrder.getOrderOtherId() == null ? null
							: purOrder.getOrderOtherId().trim());// 订单编号(来源于其他系统)
					if (!"".equals(order_other_id) && order_other_id != null) {
						saleOrder.setPurOrderId(Long.valueOf(order_other_id));
					}
					saleOrder.setOrderTime(new Date());// 订单日期

					// 业务5.销售子表回写+采购子表保存
					List<SaleOrderDetail> saleOrderDetailList = saleOrder.getDetailEntityList();
					int lineNum = 0;
					
					// 业务5.1自动取价——采购订单金额的生成：如果能匹配到唯一一条合同则对金额赋值
					// 金额：
					BigDecimal totalAmount = new BigDecimal(0);
					// 含税金额：tax_amount
					BigDecimal totalTax_amount = new BigDecimal(0);
					
					//2.根据订单信息查询可用的合同物料行
					//1）合同的供应商与订单相同
					//2）合同的适应组织范围为全部，或者合同适应组织范围包含订单采购组织
					//3）其他条件：合同已生效、租户、合同未失效、物料行在价格有效期内
					//4）物料条件：物料合同使用物料作为条件，分类合同使用分类作为条件
					// 查询合同
					ContractMaterialCtriteria ctriteria = new ContractMaterialCtriteria();
					// 业务5.2查询条件封装--供应商
					ctriteria.setSupplierid(saleOrder.getEnterpriseId());
					// 查询条件采购组织封装
					// 业务5.2查询条件封装--采购组织集合
					ctriteria.setReqOrgId(saleOrderFromDb.getOrgId());// 前端saleOrder没有传递orgid。saleOrderFromDb 自己根据id查询的实体；
					ctriteria.setEnterpriseId(saleOrder.getPurEnterpriseId());
					
					// 业务5.2查询条件封装--物料id集合
					//物料或者物料分类条件：物料合同使用物料作为条件，分类合同使用分类作为条件
					// 物料id集合
					List<String> materialIdlist = new ArrayList<String>();
					List<Long> materialClassIdlist = new ArrayList<Long>();
					for (SaleOrderDetail saleorderDetail : saleOrderDetailList) {
						if(saleorderDetail.getProductIid()!=null){
							materialIdlist.add(saleorderDetail.getProductIid());
							materialClassIdlist.add(saleorderDetail.getMaterialclassId());
						}
					}
					// 查询条件货物id封装
					if (materialIdlist != null && materialIdlist.size() > 0) {
						ctriteria.addExtField("materialIdlist", materialIdlist);
					}
					if (materialClassIdlist != null && materialClassIdlist.size() > 0) {
						ctriteria.addExtField("materialClassIdlist", materialClassIdlist);
					}
					// 业务5.3 采购组织+供应商+物料”查询对应的有效期内的合同清单；
					// 采购订单手工取价有相同代码；查询条件、业务逻辑修改时不要忘记；（方法名：getContractByPurOrder）
					// 采购组织+供应商+物料”查询对应的有效期内的合同清单；
					logger.error("合同取价条件：ctriteria=【】" + JsonUtils.toJson(ctriteria));
					List<ContractMaterial> ctMList = iAgreement2OrderService.getContractMaterialByCriteriaForVMI(ctriteria);

					//3.查询合同信息
					// 业务5.4按物料将查询的结果统一包装
//					Map<Long, List<ContractMaterial>> ctmId2ctmMap = new HashMap<Long, List<ContractMaterial>>();
					
					//付款协议：1封装合同的ids
					List<Long> listContractIds = new ArrayList<Long>();
					// 合同主表信息
					Map<Long,Contract> id2ContractMap = new HashMap<Long,Contract>();
					if(ctMList!=null && ctMList.size()>0){
						List<Long> ctIds = new ArrayList<Long>();
						for (ContractMaterial contractMaterial : ctMList) {
							ctIds.add(contractMaterial.getContractId());
							//获取合同id集合
							listContractIds.add(contractMaterial.getContractId());
							
						}
						
						if (ctIds != null && ctIds.size() != 0) {
							List<Contract> contractList = contractService.batchSelectContractById(ctIds);
							if(contractList!=null && contractList.size()>0){
								for(Contract contract : contractList){
									id2ContractMap.put(contract.getId(), contract);
								}
							}
						}
					}
					
					//付款协议：2根据合同的ids查询出来所有的付款协议，并封装map
					Map<Long,List<ContractPayTerm>> mapContractPayTerm = getMapFromContractIds(listContractIds);
					
					logger.error(JsonUtils.toJson(ctriteria) + "orgId为：" + saleOrderFromDb.getOrgId());
					
					//付款协议：存储付款协议集合
					List<ContractPayTerm> listOfContractPayTerm = new ArrayList<ContractPayTerm>();
					//付款协议：用来区分判断是否为第一个物料（只有第一个物料的付款协议才有效）
					int insertPayTerm = 0;
					Map <Long,Boolean> mapInsertFalg = new HashMap<Long,Boolean>();
					
					//1.循环处理销售订单的没一行，生成采购订单表体
					for (SaleOrderDetail saleorderDetail : saleOrderDetailList) {
						// 业务5.5 销售子表赋值过程；
						PurOrderDetail purOrderDetail = this.getPurorderDetailBySaleOrder(saleOrderFromDb,saleorderDetail);
						purOrderDetail.setEnterpriseId(purOrder.getEnterpriseId());
						purOrderDetail.setLineNum(++lineNum);// 行号
						// 业务5.6方式选择：
						// 生成采购订单时自动根据采购合同取价；如对应多个合同则支持采购员手工选取合同；如无对应合同则支持采购员手工填价；
						// 每个物料行匹配到的合同物料集合
						List<ContractMaterial> matchedCtmList = new ArrayList<ContractMaterial>();
						Long materialId = saleorderDetail.getProductIid()==null ? null : Long.parseLong(saleorderDetail.getProductIid());
						Long materialClassId = saleorderDetail.getMaterialclassId();
						//循环处理每个合同行，如果物料（物料合同）或者物料分类（分类合同）能够跟订单匹配，即可匹配上
						for (ContractMaterial contractMaterial : ctMList) {
							Long contractId = contractMaterial.getContractId();
							Contract contract = id2ContractMap.get(contractId);
							
							boolean isMatched = false;
							String isMaterialClass = (contract.getIsMaterialClass()==null||"".equals(contract.getIsMaterialClass().trim())) 
				                		? "0" : contract.getIsMaterialClass();
				            if("0".equals(isMaterialClass)){
				            	//物料合同,并且订单上的物料和合同上的物料相同
				            	if(materialId !=null && materialId.equals(contractMaterial.getMaterialId())){
				            		isMatched = true;
				            	}
				            }else if("1".equals(isMaterialClass) && contractMaterial.getMaterialClassId()!=null){
				            	if(materialClassId != null && materialClassId.equals(contractMaterial.getMaterialClassId())){
				            		isMatched = true;
				            	}
			                }else{
			                	continue;
			                }
				            //没有匹配上，继续循环下一行
				            if(!isMatched){
				            	continue;
				            }
				            
				            //物料能够匹配上，继续匹配数量
				            //业务2.如果匹配到合同
				    		//计算得到可执行数量
				    		Map <Boolean, BigDecimal> ctMatchedMap = this.setAccQuantity(contract, contractMaterial, null);
				    		//得到合同是否控制超量，及控制超量时，剩余可执行数量
				    		if(ctMatchedMap.containsKey(true)){
				    			//不控制超量，即可以无限执行：匹配成功
				    			matchedCtmList.add(contractMaterial);
				    		}else{
				    			//控制超量，得到剩余可执行数量
				    			BigDecimal enableExecNum = ctMatchedMap.get(false);
				    			//剩余可执行数量与订单数量比较，看是否能够覆盖订单数量
				    			//订单数量
				    			BigDecimal purOrderDetailQuantity = saleorderDetail.getQuantity();
				    			if(enableExecNum!=null && purOrderDetailQuantity!=null ){
				    				if(enableExecNum.compareTo(purOrderDetailQuantity)>=0){
				    					matchedCtmList.add(contractMaterial);
				    				}
				    			}
				    		}
						}
						
						//付款协议：3 给第一条物料绑定关系，后续取出第一条物料对应的合同对应的付款协议(需求：付款协议只要第一个物料行的)
						if(insertPayTerm == 0){
							mapInsertFalg.put(saleorderDetail.getId(),true);
						}else{
							mapInsertFalg.put(saleorderDetail.getId(),false);
						}
						insertPayTerm++;
						
						
						//匹配到了唯一一个数量能够覆盖订单数量的合同，执行自动取价
						if(matchedCtmList!=null && matchedCtmList.size()==1&&matchedCtmList.get(0)!=null){
							ContractMaterial ctm = matchedCtmList.get(0);
							Contract contract = id2ContractMap.get(ctm.getContractId());
							//执行合同已完成量的回写&&判断是否可超量执行
							// 业务5.8 如果匹配到合同，需要同时将已完成数量进行回写；
							// 1.是否允许回写数量；2.如果不允许则判断可执行数量是否覆盖输入数量；
							Boolean insertFlag  = this.updateAccomplishedQuantity(ctm, contract, purOrderDetail);
							if(insertFlag){
								// 业务5.7 采购订单、销售订单子表赋值（金额、品牌、合同id等信息）
								Map<String,Object>mapObject = this.setPurPropertysFromContractMaterial(saleorderDetail, purOrderDetail, contract, ctm);
								saleorderDetail = (SaleOrderDetail)mapObject.get("saleOrderDetail");
								purOrderDetail = (PurOrderDetail)mapObject.get("purOrderDetail");
								//付款协议：3.5取出第一个物料行的付款协议
								if (mapInsertFalg != null && mapInsertFalg.get(saleorderDetail.getId()) != null
										&& mapInsertFalg.get(saleorderDetail.getId())) {
									//此时采购订单还没有存储，所以不能以采购订单的id为key；但是可以通过销售订单子表-采购订单子表-合同做唯一关联；
									listOfContractPayTerm = mapContractPayTerm.get(purOrderDetail.getContractId());
								}
							}else{
								ifAutoConfirm = false;
							}
						}else{
							ifAutoConfirm = false;
						}
						
						// 业务5.9 金额计算；
						BigDecimal price = purOrderDetail.getPrice();// 无税金额
						BigDecimal taxPrice = purOrderDetail.getTaxPrice();//含税金额
						BigDecimal quantity = purOrderDetail.getQuantity();//数量
						if ((price != null && quantity != null) || (taxPrice != null && quantity != null)) {

							BigDecimal money = price.multiply(quantity);// 无税
							BigDecimal taxMoney = taxPrice.multiply(quantity);// 含税无税

							totalAmount = totalAmount.add(money);// 无税
							totalTax_amount = totalTax_amount.add(taxMoney);// 含税

							// 子表金额赋值
							purOrderDetail.setAmount(money.setScale(2, BigDecimal.ROUND_HALF_UP));// 无税
							purOrderDetail.setTaxAmount(taxMoney.setScale(2, BigDecimal.ROUND_HALF_UP));// 含税

							saleorderDetail.setAmount(money.setScale(2, BigDecimal.ROUND_HALF_UP));// 金额
							saleorderDetail.setTaxAmount(taxMoney.setScale(2, BigDecimal.ROUND_HALF_UP));// 含税金额
						}

						// 业务6.  采购订单子表的保存
						PurOrderDetail purOrderDetailTeap = purOrderService.savePurOrderDetailForVMI(purOrder,
								purOrderDetail);
						// 业务7. 采购订单子表id回写
						saleorderDetail.setPurOrderDetailId(purOrderDetailTeap.getId());
						saleorderDetail.setPurOrderId(purOrder.getId());
					}
					
					// 业务8. 总金额赋值(表头2位、表体2位)
					saleOrder.setNotaxMoney(totalAmount.setScale(2, BigDecimal.ROUND_HALF_UP));//无税总金额
					saleOrder.setTotalMoney(totalTax_amount.setScale(2, BigDecimal.ROUND_HALF_UP));// 总金额
					purOrder.setNotaxMoney(totalAmount.setScale(2, BigDecimal.ROUND_HALF_UP));
					purOrder.setTotalMoney(totalTax_amount.setScale(2, BigDecimal.ROUND_HALF_UP));// 总金额

					//付款协议：4 根据付款协议字段转换(含金额处理：保留2位)
					if(listOfContractPayTerm !=null && listOfContractPayTerm.size()>0){
						Map mapResultPayTerm = this.getSaleOrderPayTerm(listOfContractPayTerm,purOrder,saleOrder);
						if(mapResultPayTerm!=null && mapResultPayTerm.get("saleOrderList")!=null && mapResultPayTerm.get("purOrderList") !=null ){
							saleOrder.setSaleOrderPayTermList((List)mapResultPayTerm.get("saleOrderList"));
							purOrder.setPurOrderPayTermList((List)mapResultPayTerm.get("purOrderList"));
						}
					}
					
					// 业务9.销售订单的保存(主、子一起)//update作用：订单号、金额回写、采购信息回写，订单状态
					ServiceResponse<Integer> s = saleOrderService.updateSaleOrderForVMI(saleOrder);
					// 业务10.采购订单主表的价格保存
					purOrderService.updatePurOrderForVMI(purOrder);

					// 业务11.采购订单自动确认
					//REMARK:(~.~~.~~.~~.~下面还有一处。要改一起改~.~~.~~.~~.~~.~搜索"confirmPurchaseOrder")
					if(ifAutoConfirm){
						JSONObject returnRes = this.confirmPurOrder(purOrder, user);
						logger.error("此物料明细行所对应的合同可执行量大于申请数量，可以自动赋值和自动确认");
						logger.error(JsonUtils.toJson(returnRes));
					}else{
						logger.error("此物料明细行所对应的合同可执行量小于申请数量 或者 合同没有取到，不能自动赋值和自动确认");
					}
					

					result.put("data", s.getResult());
					result.put("status", "1");
					result.put("msg", s.getMsg());
				} else {
					result.put("msg", "提交方式传递不合法");
				}
			} else {
				message += "请检查修正后重新保存或提交。";
				result.put("status", 0);
				result.put("msg", message);
				return result;
			}

		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", 0);
			result.put("msg", e.getMessage());
		}
		return result;
	}

	@Override
	public JSONObject deleteSaleOrder(String jsonStr) {// 销售订单删除
		// 业务逻辑：1.删除
		JSONObject result = new JSONObject();

		if (!"".equals(jsonStr) && jsonStr != null) {
			// 处理请求参数
			SaleOrder saleOrder = JSONObject.parseObject(jsonStr, SaleOrder.class);

			if (saleOrder != null) {
				try {
					ServiceResponse s = saleOrderService.deleteSaleOrderForVMI(saleOrder);
					result.put("data", s.getResult());
					result.put("status", s.getCode());
					result.put("msg", s.getMsg());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					result.put("status", 0);
					result.put("msg", e.getMessage());
				}
			} else {
				result.put("status", 0);
				result.put("msg", "前端json串转换SaleOrder失败");
			}
		} else {
			result.put("status", 0);
			result.put("msg", "前端json为空");
		}
		return result;
	}

	@Override
	public JSONObject discardSaleOrder(String jsonStr) {// 废弃--状态：18：待发布--待需求完善；

		JSONObject result = new JSONObject();

		if (!"".equals(jsonStr) && jsonStr != null) {
			// 处理请求参数
			SaleOrder saleOrder = JSONObject.parseObject(jsonStr, SaleOrder.class);

			if (saleOrder != null) {
				try {
					// 1.销售订单修改状态
					String orderStatuss = SaleOrderSplitEnum.OBSOLETECOUNT.getCode();
					saleOrder.setOrderStatus(orderStatuss);
					ServiceResponse s = saleOrderService.publishSaleOrderForVMI(saleOrder);
					// 2.根据销售订单查询采购订单
					Long purOrderId = saleOrder.getPurOrderId();
					if (purOrderId != null) {
						PurOrderCriteria criteria = new PurOrderCriteria();
						criteria.setId(purOrderId);
						List<PurOrder> purOrderList = purOrderService.selectPurOrderByPurOrderCriteria(criteria);
						if (purOrderList.size() > 0) {
							PurOrder one = purOrderList.get(0);

							one.setOrderStatus(PurOrderSplitEnum.OBSOLETECOUNT.getCode());
							// 3.采购订单保存
							purOrderService.updatePurOrderForVMI(one);
						}
					} else {
						result.put("msg", "ID为：" + saleOrder.getId() + "的销售订单无采购订单id，无法执行修改采购订单状态的操作");
					}
					result.put("data", s.getResult());
					result.put("status", s.getCode());
					result.put("msg", s.getMsg());
				} catch (Exception e) {
					e.printStackTrace();
					result.put("msg", e.getMessage());
				}
			} else {
				result.put("msg", "前端json串转换SaleOrder失败");
			}
		} else {
			result.put("msg", "前端json为空");
		}
		return result;
	}

	@Override
	public JSONObject canclePublishSaleOrder(String jsonStr) {// 取消发布
		JSONObject result = new JSONObject();
		if (!"".equals(jsonStr) && jsonStr != null) {
			// 处理请求参数
			SaleOrder saleOrder = JSONObject.parseObject(jsonStr, SaleOrder.class);
			if (saleOrder != null) {
				try {
					// 业务使用场景：拒绝、待买方确认可以点击取消发布；
					// 0.删除采购订单
					SaleOrderCriteria saleOrderCriteria = new SaleOrderCriteria();
					saleOrderCriteria.setId(saleOrder.getId());
					saleOrder = getSaleOrderObj(saleOrderCriteria);

					Long purOrderId = saleOrder.getPurOrderId();
					PurOrderCriteria criteria = new PurOrderCriteria();
					criteria.setId(purOrderId);
					if (purOrderId != null) {
						// 删除
						ServiceResponse deleteFlag = purOrderService.deletePurOrder4VMI(criteria);
						if ("1".equals(deleteFlag.getCode())) {
							// 删除成功
							// 1.将销售订单修改为待发布
							String orderStatuss = SaleOrderSplitEnum.TOBERELEASED.getCode();
							saleOrder.setOrderStatus(orderStatuss);
							ServiceResponse s = saleOrderService.publishSaleOrderForVMI(saleOrder);
							result.put("data", s.getResult());
							result.put("status", s.getCode());
							result.put("msg", s.getMsg());
						} else {
							result.put("status", 0);
							result.put("msg", "销售订单取消发布失败");
						}

					} else {
						result.put("status", 0);
						result.put("msg", "此销售订单没有与之对应的采购订单实体");
					}

				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e.getMessage(), e);
					result.put("status", 0);
					result.put("msg", e.getMessage());
				}
			} else {
				result.put("status", 0);
				result.put("msg", "前端json串转换SaleOrder失败");
			}
		} else {
			result.put("status", 0);
			result.put("msg", "前端json为空");
		}
		return result;
	}

	@Override
	public JSONObject refuseOrder(String jsonStr, MgrUser user, String buyerConfirOpinion) {// 采购订单拒绝

		JSONObject result = new JSONObject();

		if (!"".equals(jsonStr) && jsonStr != null) {
			// 处理请求参数
			PurOrder purOrder = JSONObject.parseObject(jsonStr, PurOrder.class);

			if (purOrder != null) {
				try {
					// 业务：1.修改采购订单的orderStatus 为
					// PurOrderSplitEnum.BUYERREFUSAL("18","买方已拒绝")
					purOrder.setOrderStatus(PurOrderSplitEnum.BUYERREFUSAL.getCode());
					// 2.增加确认时间、确认人、确认电话、确认意见的字段赋值
					if (user != null) {
						purOrder.setBuyerConfirId(user.getId());
						purOrder.setBuyerConfirMobile(user.getUserMobile());
						purOrder.setBuyerConfirName(user.getName());
					}
					purOrder.setBuyerConfirOpinion(buyerConfirOpinion);
					purOrder.setBuyerConfirTime(new Date());

					// 3.保存采购订单
					ServiceResponse<Integer> s = purOrderService.updatePurOrderForVMI(purOrder);// 保存全部字段、确认信息、订单状态、交易类型；
					// 4.根据采购信息得到销售订单
					SaleOrderCriteria criteria = new SaleOrderCriteria();
					criteria.setPurOrderId(purOrder.getId());
					// 5.得到销售订单
					SaleOrder saleOrder = this.getSaleOrderFromPurOrder(purOrder);
					if (saleOrder != null) {
						// 确认信息赋值
						if (user != null) {
							saleOrder.setBuyerConfirId(user.getId());
							saleOrder.setBuyerConfirMobile(user.getUserMobile());
							saleOrder.setBuyerConfirName(user.getName());
						}
						saleOrder.setBuyerConfirOpinion(buyerConfirOpinion);
						saleOrder.setBuyerConfirTime(new Date());
						// 订单状态
						saleOrder.setOrderStatus(SaleOrderSplitEnum.BUYERREFUSAL.getCode());
						// 5.销售订单数据回写
						saleOrderService.updateSaleOrderForVMI(saleOrder);
						// 6.反馈
						result.put("status", "1");
						result.put("msg", "采购订单拒绝成功！");
					} else {
						result.put("status", 0);
						result.put("msg", "根据采购订单获取销售订单失败！");
					}
				} catch (Exception e) {
					e.printStackTrace();
					result.put("status", 0);
					result.put("msg", e.getMessage());
				}
			} else {
				result.put("status", 0);
				result.put("msg", "前端json串转换PurOrder失败");
			}
		} else {

			result.put("status", 0);
			result.put("msg", "前端json为空");
		}
		return result;
	}

	@Override
	public JSONObject confirmPurchaseOrder(String jsonStr, MgrUser user, String buyerConfirOpinion) {// 采购商订单确认服务
		//REMARK:(~.~~.~~.~~.~关于流程的走向判断，以及是否发送ERP下面还有一处。要改一起改~.~~.~~.~~.~~.~搜索"confirmPurOrder")
		JSONObject result = new JSONObject();

		if (jsonStr == null || "".equals(jsonStr)) {
			result.put("status", 0);
			result.put("msg", "前端json为空");
			return result;
		}

		// 处理请求参数
		PurOrder purOrder = JSONObject.parseObject(jsonStr, PurOrder.class);

		if (purOrder == null) {
			result.put("msg", "前端json串转换PurOrder失败");
			result.put("status", 0);
			return result;
		}

		try {
			// 业务0.校验订单分类必填；
			if (purOrder.getTransactionTypeId() == null || "".equals(purOrder.getTransactionTypeId())
					|| purOrder.getTransactionTypeCode() == null || "".equals(purOrder.getTransactionTypeCode())) {
				result.put("msg", "请选择交易类型！");
				result.put("status", 0);
				return result;
			}

			// 业务1.校验每个明细是否存在
			List<PurOrderDetail> purorderDetailList = purOrder.getPurOrderDetailList();
			if (purorderDetailList == null || purorderDetailList.size() == 0) {
				result.put("status", 0);
				result.put("msg", "采购订单明细为空");
				return result;
			}
			
			//业务1.1校款协议校验
			List<PurOrderPayTerm> purOrderPayTermList = purOrder.getPurOrderPayTermList();
			if (purOrderPayTermList != null && purOrderPayTermList.size() > 0) {
				ServiceResponse<Integer> response1 = checkPayTerm(purOrderPayTermList,purOrder);
				if (response1 != null && response1.getCode() !=null && response1.getCode().equals("false")) {
					result.put("status", 0);
					result.put("msg", response1.getMsg());
					return result;
				}
			}

			// 1.补货订单申请生效流程判断 采购订单流程走向；
			// 第一种：
			// YC补货申请订单->买方确认申请订单->买方YC审批：
			// 如果有审批流则启动审批流，审批通过后采购订单和销售订单自动变为“买方已审批”，审批不通过则采购订单和销售订单状态变为“买方已拒绝
			// 判断是否有符合条件的云采VMI订单审批流，如果没有采购订单状态直接变为“买方已审批”，对应的销售订单状态也变为“买方已审批，
			String busiType = purOrder.getBusitype();
			if (busiType == null || "".equals(busiType)) {
				result.put("msg", "采购订单无订单申请生效流程配置");
				result.put("status", 0);
				return result;
			}

			//校验单价是否存在
			//判断单价为空的时候，判断一下取价的方式，如果是NC取价就放开了 
			Config[] configs = iVmiConfigService.queryByEnterpriseId(purOrder.getEnterpriseId());
			if(configs!=null){
				String priceSrcType = configs[0].getPriceSrcType();
				if (!"".equals(priceSrcType) && priceSrcType != null && !"0".equals(priceSrcType)) {
					for (int i = 0; i < purorderDetailList.size(); i++) {
						//单价的判断
						PurOrderDetail purorderDetail = purorderDetailList.get(i);
						BigDecimal price = purorderDetail.getPrice();
						if (price == null) {
							result.put("status", 0);
							result.put("msg", "物料：" + purorderDetail.getProductName() + "的价格为空，请重新填！");
							return result;
						}
					}
					
					List<Long> ctMIdList = new ArrayList<Long>();
					List<Long> ctIdList = new ArrayList<Long>();
					for(PurOrderDetail purorderDetail : purorderDetailList){
						if(purorderDetail.getContractMaterialId()!=null && purorderDetail.getContractId()!=null){
							ctMIdList.add(purorderDetail.getContractMaterialId());
							ctIdList.add(purorderDetail.getContractId());
						}
					}
					Map<Long,ContractMaterial> id2CtmMap = new HashMap<Long, ContractMaterial>();
					Map<Long,Contract> id2CtMap = new HashMap<Long, Contract>();
					if(ctMIdList!=null && ctMIdList.size()>0 && ctIdList!=null && ctIdList.size()>0){
						List<ContractMaterial> ctmList = contractMaterialService.getContractMaterialByIds(ctMIdList);
						if(ctmList!=null && ctmList.size()>0){
							for(ContractMaterial ctm : ctmList){
								id2CtmMap.put(ctm.getId(), ctm);
							}
						}
						List<Contract> ctList = contractService.batchSelectContractById(ctIdList);
						if(ctList!=null && ctList.size()>0){
							for(Contract ct : ctList){
								id2CtMap.put(ct.getId(), ct);
							}
						}
					}
					
					for(PurOrderDetail purorderDetail : purorderDetailList){
						//业务1.1.执行数量的回写
						Long ctMId = purorderDetail.getContractMaterialId();
						ContractMaterial ctm = ctMId==null ? null : id2CtmMap.get(ctMId);
						Long ctId = purorderDetail.getContractId();
						Contract ct = id2CtMap.get(ctId);
						if(ctm!=null && ct!=null){
							//TODO:for循环里的查询我已经处理，请批量处理回写
							this.updateAccomplishedQuantity(ctm, ct, purorderDetail);
						}
					}
				}
			}

			// 根据采购订单获取销售订单并给销售订单订单状态赋值
			String saleOrderStatus = "";
			String orderStatus = "";
			Boolean need2ERP = false;

			PurOrderCriteria criteria = new PurOrderCriteria();
			criteria.setId(purOrder.getId());
			//用采购商这边确认，purchaseId从前端传递为空，这里需要查一下赋值，才能正常进入审批流；
			PurOrder purOrderTemp = this.getPurOrder(criteria);
			purOrder.setPurchaseId(purOrderTemp.getPurchaseId());

			boolean needCommit = false;
			// 业务1.1审批流判断：
			Map<String, Boolean> approveEnableMap = purOrderService.getProcess(purOrder);
			if (OrderBusiTypeEnum.VMIAPPLY_PURYCCONFIRM_PURYCAPPROVE.getCode().equals(busiType)) {
				// 审批流查看结果
				if (approveEnableMap != null && approveEnableMap.containsKey(BillTypeEnum.YCORDER.getCode())
						&& approveEnableMap.get(BillTypeEnum.YCORDER.getCode())) {
					// 如果配置审批流:
					// 提交审批流
					needCommit = true;

					// 订单状态改成买方审批中
					saleOrderStatus = SaleOrderSplitEnum.PURAPPROVINGCOUNT.getCode();
					orderStatus = PurOrderSplitEnum.PURAPPROVINGCOUNT.getCode();

					result.put("status", "1");
					result.put("msg", "采购单提交审批流");
				} else {
					// 没有配置审批流
					// 修改采购订单订单状态：("10", "买方已审批"),
					orderStatus = PurOrderSplitEnum.PURAPPROVEDCOUNT.getCode();
					// 修改销售订单状态("10", "买方已审批")
					saleOrderStatus = SaleOrderSplitEnum.PURAPPROVEDCOUNT.getCode();
					result.put("status", "1");
					result.put("msg", "采购单确认成功！");
					// 根据采购订单查找销售订单
				}
			} else if (OrderBusiTypeEnum.VMIAPPLY_PURYCCONFIRM_PURYCAPPROVE_ERORDERAPPROVE.getCode().equals(busiType)) {
				// 第二种：
				// YC补货申请订单->买方确认申请订单->买方YC审批->ERP审批态订单：
				// 如果有审批流，则启动审批流，审批通过后采购订单和销售订单自动变为“买方已审批”并将订单写入ERP生成ERP已审批的采购订单，审批拒绝后采购订单和销售订单状态都变为“买方已拒绝”。注意：此处需要预留集成ERP失败的原因显示和重新汇入ERP的按钮。（交互形式按原有）
				// 判断是否有符合条件的云采VMI订单审批流，如果没有采购订单状态直接变为“买方已审批”并将订单写入ERP生成ERP已审批的采购订单

				// 审批流查看结果
				if (approveEnableMap != null && approveEnableMap.containsKey(BillTypeEnum.YCORDER.getCode())
						&& approveEnableMap.get(BillTypeEnum.YCORDER.getCode())) {
					// 如果配置审批流
					needCommit = true;
					// 订单状态改成买方审批中
					saleOrderStatus = SaleOrderSplitEnum.PURAPPROVINGCOUNT.getCode();
					orderStatus = PurOrderSplitEnum.PURAPPROVINGCOUNT.getCode();

					result.put("status", "1");
					result.put("msg", "采购单提交审批流");
					// result.put("data", purOrder);
					// 审批拒绝后采购订单和销售订单状态都变为“买方已拒绝”--------
				} else {
					// 没有配置审批流:
					// 1.则修改订单状态为：("10", "买方已审批"),
					orderStatus = PurOrderSplitEnum.PURAPPROVEDCOUNT.getCode();
					// 修改销售订单状态("10", "买方已审批")
					saleOrderStatus = SaleOrderSplitEnum.PURAPPROVEDCOUNT.getCode();

					// 2.写入ERP生成ERP已审批的采购订单
					need2ERP = true;
					// PurOrder purorder = service.publish(purOrder.getId());
					result.put("status", "1");
					result.put("msg", "确认成功");
				}
			} else if (OrderBusiTypeEnum.VMIAPPLY_PURYCCONFIRM_ERPORDERSAVE_ERAPPROVE.getCode().equals(busiType)) {
				// 2.YC补货申请订单->买方确认申请订单->ERP自有态订单->ERP订单审批：
				// 采购订单写入ERP生成自由态订单，云采的采购订单和销售订单的状态不变。
				// 等ERP中订单审批通过后云采采购订单和销售订单的状态变为“买方已审批”，
				// 如果ERP订单在ERP中修改后审批，需要同步最新的订单数据更新云采采购订单和销售订单。
				// 注意：此处需要预留集成ERP失败的原因显示和重新汇入ERP的按钮。（交互形式按原有）

				// 订单状态改成买方审批中
				saleOrderStatus = SaleOrderSplitEnum.PURAPPROVINGCOUNT.getCode();
				orderStatus = PurOrderSplitEnum.PURAPPROVINGCOUNT.getCode();
				need2ERP = true;

				// 推入ERP
				// PurOrder purorder = service.publish(purOrder.getId());
				result.put("status", "1");
				result.put("msg", "确认成功");
				// ServiceResponse<String> resultToNC =
				// purOrderService.releaseOrderToNC(criteria);

				// 需要在上述方法中判断是否是直接审批通过（ 修改状态）还是修改后审批（同步修改采购、销售订单）
			} else if (OrderBusiTypeEnum.VMIAPPLY_PURYCCONFIRM_ERPORDERAPPROVE.getCode().equals(busiType)) {
				// YC补货申请订单->买方确认申请订单->ERP审批态订单：
				// 采购订单写入ERP生成审批态的订单，云采的采购订单和销售订单的状态变为“买方已审批”；
				// 注意：此处需要预留集成ERP失败的原因显示和重新汇入ERP的按钮。（交互形式按原有）

				// 推入ERP
				// 订单状态改成买方审批中
				saleOrderStatus = SaleOrderSplitEnum.PURAPPROVINGCOUNT.getCode();
				orderStatus = PurOrderSplitEnum.PURAPPROVINGCOUNT.getCode();

				need2ERP = true;
				result.put("status", "1");
				result.put("msg", "确认成功");
				// 需要在上述方法中修改两个订单的状态
			} else {
				result.put("msg", "采购订单无订单申请生效流程配置无" + busiType + "此配置");
				result.put("status", 0);
				return result;
			}

			// 确认信息赋值
			purOrder.setBuyerConfirOpinion(buyerConfirOpinion);
			if (user != null) {
				purOrder.setBuyerConfirId(user.getId());
				purOrder.setBuyerConfirMobile(user.getUserMobile());
				purOrder.setBuyerConfirName(user.getName());
			}

			purOrder.setBuyerConfirTime(new Date());

			// 业务3.保存
			purOrder.setOrderStatus(orderStatus);
			
			//将付款协议重新封装赋值
			purOrder = this.getPurOrderPayTermBySelf(purOrder);
			purOrderService.updatePurOrderInfo(purOrder);// 他们的方法
			
			// 销售订单状态保存
			SaleOrder saleOrder = this.getSaleOrderFromPurOrder(purOrder);
			saleOrder.setOrderStatus(saleOrderStatus);
			//将付款协议重新封装赋值
			saleOrder = getSaleOrderPayTermByPurOrder(purOrder, saleOrder);
			saleOrderService.updateSaleOrderForVMI(saleOrder);// 此方法只修改状态

			if(needCommit){
				//更新了订单的单价、金额等信息，重新查询下订单
				PurOrderCriteria purorderCriteria = new PurOrderCriteria();
				purorderCriteria.setId(purOrder.getId());
				//用采购商这边确认，purchaseId从前端传递为空，这里需要查一下赋值，才能正常进入审批流；
				PurOrder purorder2commit = this.getPurOrder(purorderCriteria);
				purOrder = purOrderService.purOrderCommitAction4VMI(purorder2commit);
				//更新下审批流状态，不需要更新表体，把表体置空
				purOrder.setBillstatus(Integer.valueOf(BillStatusEnum.APPROVIND.getCode()));
				purOrder.setPurOrderDetailList(null);
				purOrderService.updatePurOrderInfo(purOrder);
			}
			// 如果需要发送ERP，自动发送ERP
			if (need2ERP) {
				PurOrder purorder = service.publish(purOrder.getId());
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("msg", "订单确认出错，错误信息为：{}" + e.getMessage());
			result.put("status", 0);
		}
		return result;
	}

	/**
	 * @Title:
	 * @param:@param purOrder
	 * @return:
	 * @Description: 销售订单发布之后的采购订单自动确认
	 * @author:liuhao7@yonyou.com
	 * @date:2019年11月12日
	 */
	private JSONObject confirmPurOrder(PurOrder purOrder, MgrUser user) {
		// TODO:传入的purOrder 由于之前的写法，导致purOrder只有表头，这里需要再根据id将完整的purOrder查出来；
		// TODO:同时下面的推NC的需要criteria 对象；
		PurOrderCriteria criteria = new PurOrderCriteria();
		criteria.setId(purOrder.getId());

		// 获取完整的采购订单对象
		purOrder = this.getPurOrder(criteria);

		JSONObject result = new JSONObject();

		if (purOrder == null) {
			result.put("msg", "采购订单实体为空");
			result.put("status", 0);
			return result;
		}

		try {
			// 业务0.校验订单分类必填；
			if (purOrder.getTransactionTypeId() == null || "".equals(purOrder.getTransactionTypeId())
					|| purOrder.getTransactionTypeCode() == null || "".equals(purOrder.getTransactionTypeCode())) {
				result.put("msg", "请选择交易类型！");
				result.put("status", 0);
				return result;
			}

			// 业务1.校验每个明细的单价
			List<PurOrderDetail> purorderDetailList = purOrder.getPurOrderDetailList();
			if (purorderDetailList == null || purorderDetailList.size() == 0) {
				result.put("status", 0);
				result.put("msg", "采购订单明细为空");
				return result;
			}

			// 1.补货订单申请生效流程判断 采购订单流程走向；
			// 第一种：
			// YC补货申请订单->买方确认申请订单->买方YC审批：
			// 如果有审批流则启动审批流，审批通过后采购订单和销售订单自动变为“买方已审批”，审批不通过则采购订单和销售订单状态变为“买方已拒绝
			// 判断是否有符合条件的云采VMI订单审批流，如果没有采购订单状态直接变为“买方已审批”，对应的销售订单状态也变为“买方已审批，
			String busiType = purOrder.getBusitype();
			if (busiType == null || "".equals(busiType)) {
				result.put("msg", "采购订单无订单申请生效流程配置");
				result.put("status", 0);
				return result;
			}

			for (int i = 0; i < purorderDetailList.size(); i++) {
				PurOrderDetail purorderDetail = purorderDetailList.get(i);
				BigDecimal price = purorderDetail.getPrice();
				if (price == null) {
					result.put("status", 0);
					result.put("msg", "物料：" + purorderDetail.getProductName() + "的价格为空，请重新填！");
					return result;
				}
			}

			// 根据采购订单获取销售订单并给销售订单订单状态赋值
			String saleOrderStatus = "";
			String orderStatus = "";
			Boolean need2ERP = false;
			Boolean needCommit =false;

			// 业务1.1审批流判断：
			Map<String, Boolean> approveEnableMap = purOrderService.getProcess(purOrder);
			if (OrderBusiTypeEnum.VMIAPPLY_PURYCCONFIRM_PURYCAPPROVE.getCode().equals(busiType)) {
				// 审批流查看结果
				if (approveEnableMap != null && approveEnableMap.containsKey(BillTypeEnum.YCORDER.getCode())
						&& approveEnableMap.get(BillTypeEnum.YCORDER.getCode())) {
					// 如果配置审批流:
					// 提交审批流
					needCommit = true;

					// 订单状态改成买方审批中
					saleOrderStatus = SaleOrderSplitEnum.PURAPPROVINGCOUNT.getCode();
					orderStatus = PurOrderSplitEnum.PURAPPROVINGCOUNT.getCode();

					result.put("status", "1");
					result.put("msg", "采购单提交审批流");
				} else {
					// 没有配置审批流
					// 修改采购订单订单状态：("10", "买方已审批"),
					orderStatus = PurOrderSplitEnum.PURAPPROVEDCOUNT.getCode();
					// 修改销售订单状态("10", "买方已审批")
					saleOrderStatus = SaleOrderSplitEnum.PURAPPROVEDCOUNT.getCode();
					result.put("status", "1");
					result.put("msg", "采购单确认成功！");
					// 根据采购订单查找销售订单
				}
			} else if (OrderBusiTypeEnum.VMIAPPLY_PURYCCONFIRM_PURYCAPPROVE_ERORDERAPPROVE.getCode().equals(busiType)) {
				// 第二种：
				// YC补货申请订单->买方确认申请订单->买方YC审批->ERP审批态订单：
				// 如果有审批流，则启动审批流，审批通过后采购订单和销售订单自动变为“买方已审批”并将订单写入ERP生成ERP已审批的采购订单，审批拒绝后采购订单和销售订单状态都变为“买方已拒绝”。注意：此处需要预留集成ERP失败的原因显示和重新汇入ERP的按钮。（交互形式按原有）
				// 判断是否有符合条件的云采VMI订单审批流，如果没有采购订单状态直接变为“买方已审批”并将订单写入ERP生成ERP已审批的采购订单

				// 审批流查看结果
				if (approveEnableMap != null && approveEnableMap.containsKey(BillTypeEnum.YCORDER.getCode())
						&& approveEnableMap.get(BillTypeEnum.YCORDER.getCode())) {
					// 如果配置审批流
					needCommit = true;
					// 订单状态改成买方审批中
					saleOrderStatus = SaleOrderSplitEnum.PURAPPROVINGCOUNT.getCode();
					orderStatus = PurOrderSplitEnum.PURAPPROVINGCOUNT.getCode();

					result.put("status", "1");
					result.put("msg", "采购单提交审批流");
					// result.put("data", purOrder);
					// 审批拒绝后采购订单和销售订单状态都变为“买方已拒绝”--------
				} else {
					// 没有配置审批流:
					// 1.则修改订单状态为：("10", "买方已审批"),
					orderStatus = PurOrderSplitEnum.PURAPPROVEDCOUNT.getCode();
					// 修改销售订单状态("10", "买方已审批")
					saleOrderStatus = SaleOrderSplitEnum.PURAPPROVEDCOUNT.getCode();

					// 2.写入ERP生成ERP已审批的采购订单
					need2ERP = true;
					result.put("status", "1");
					result.put("msg", "确认成功");
				}
			} else if (OrderBusiTypeEnum.VMIAPPLY_PURYCCONFIRM_ERPORDERSAVE_ERAPPROVE.getCode().equals(busiType)) {
				// 2.YC补货申请订单->买方确认申请订单->ERP自有态订单->ERP订单审批：
				// 采购订单写入ERP生成自由态订单，云采的采购订单和销售订单的状态不变。
				// 等ERP中订单审批通过后云采采购订单和销售订单的状态变为“买方已审批”，
				// 如果ERP订单在ERP中修改后审批，需要同步最新的订单数据更新云采采购订单和销售订单。
				// 注意：此处需要预留集成ERP失败的原因显示和重新汇入ERP的按钮。（交互形式按原有）

				// 订单状态改成买方审批中
				saleOrderStatus = SaleOrderSplitEnum.PURAPPROVINGCOUNT.getCode();
				orderStatus = PurOrderSplitEnum.PURAPPROVINGCOUNT.getCode();
				need2ERP = true;

				result.put("status", "1");
				result.put("msg", "确认成功");

				// 需要在上述方法中判断是否是直接审批通过（ 修改状态）还是修改后审批（同步修改采购、销售订单）
			} else if (OrderBusiTypeEnum.VMIAPPLY_PURYCCONFIRM_ERPORDERAPPROVE.getCode().equals(busiType)) {
				// YC补货申请订单->买方确认申请订单->ERP审批态订单：
				// 采购订单写入ERP生成审批态的订单，云采的采购订单和销售订单的状态变为“买方已审批”；
				// 注意：此处需要预留集成ERP失败的原因显示和重新汇入ERP的按钮。（交互形式按原有）

				// 推入ERP
				// 订单状态改成买方审批中
				saleOrderStatus = SaleOrderSplitEnum.PURAPPROVINGCOUNT.getCode();
				orderStatus = PurOrderSplitEnum.PURAPPROVINGCOUNT.getCode();

				need2ERP = true;
				
				result.put("status", "1");
				result.put("msg", "确认成功");
				// 需要在上述方法中修改两个订单的状态
			} else {
				result.put("msg", "采购订单无订单申请生效流程配置无" + busiType + "此配置");
				result.put("status", 0);
				return result;
			}

			// 确认信息赋值
			if (user != null) {
				purOrder.setBuyerConfirId(user.getId());
				purOrder.setBuyerConfirMobile(user.getUserMobile());
				purOrder.setBuyerConfirName(user.getName());
			}

			purOrder.setBuyerConfirTime(new Date());

			// 业务3.保存
			purOrder.setOrderStatus(orderStatus);
			purOrderService.updatePurOrderInfo(purOrder);// 他们的方法

			// 销售订单状态保存
			SaleOrder saleOrder = this.getSaleOrderFromPurOrder(purOrder);
			saleOrder.setOrderStatus(saleOrderStatus);
			saleOrderService.updateSaleOrderForVMI(saleOrder);// 此方法只修改状态

			//如果需要提交审批流，提交审批流
			if(needCommit){
				purOrder = purOrderService.purOrderCommitAction4VMI(purOrder);
				//更新下审批流状态，不需要更新表体，把表体置空
				purOrder.setBillstatus(Integer.valueOf(BillStatusEnum.APPROVIND.getCode()));
				purOrder.setPurOrderDetailList(null);
				purOrderService.updatePurOrderInfo(purOrder);
			}
			
			// 如果需要发送ERP，自动发送ERP
			if (need2ERP) {
				PurOrder purorder = service.publish(purOrder.getId());
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("msg", "订单确认出错，错误信息为：{}" + e.getMessage());
			result.put("status", 0);
		}

		return result;
	}

	/**
	 * 
	 * @Title:
	 * @param:@param purOrder
	 * @param:@return
	 * @return:
	 * @Description: 根据采购订单获取对应销售订单
	 * @author:liuhao7@yonyou.com
	 */
	private SaleOrder getSaleOrderFromPurOrder(PurOrder purOrder) {
		SaleOrderCriteria criteria = new SaleOrderCriteria();
		criteria.setPurOrderId(purOrder.getId());

		// 5.得到销售订单
		ServiceResponse<SaleOrder> resultList = saleOrderService.selectByPurOrderIdAndSource(criteria);
		SaleOrder saleOrder = (SaleOrder) resultList.getResult();
		//在确认的时候，如果是手工取价，需要将品牌信息回写到销售订单子表上
		List <SaleOrderDetail>saleOrderDetailList = saleOrder.getDetailEntityList();
		List <PurOrderDetail>purOrderDetailList = purOrder.getPurOrderDetailList();
		
		Map<Long,PurOrderDetail> map = new HashMap<Long,PurOrderDetail>();
		if(purOrderDetailList!=null && purOrderDetailList.size()>0){
			for(PurOrderDetail purOrderDetail :purOrderDetailList ){
				map.put(purOrderDetail.getId(), purOrderDetail);
			}
		}
		
		if(saleOrderDetailList!=null && saleOrderDetailList.size()>0){
			for(SaleOrderDetail saleOrderDetail :saleOrderDetailList){
				if(map.containsKey(saleOrderDetail.getPurOrderDetailId())){
					saleOrderDetail.setRequireBrand(((PurOrderDetail)map.get(saleOrderDetail.getPurOrderDetailId())).getRequireBrand());
					saleOrderDetail.setRequireBrandCode(((PurOrderDetail)map.get(saleOrderDetail.getPurOrderDetailId())).getRequireBrandCode());
					saleOrderDetail.setRequireBrandErpId(((PurOrderDetail)map.get(saleOrderDetail.getPurOrderDetailId())).getRequireBrandErpId());
					saleOrderDetail.setRequireBrandId(((PurOrderDetail)map.get(saleOrderDetail.getPurOrderDetailId())).getRequireBrandId());
					
					PurOrderDetail purorderDetail = map.get(saleOrderDetail.getPurOrderDetailId());
					//确认无税单价(此时取合同的单价)
					saleOrderDetail.setConPrice(purorderDetail.getPrice());
					//确认含税单价(此时取合同的含税单价)
					saleOrderDetail.setConTaxPrice(purorderDetail.getTaxPrice());
					saleOrderDetail.setTaxrate(purorderDetail.getTaxrate());
					saleOrderDetail.setAmount(purorderDetail.getAmount());
					saleOrderDetail.setTaxAmount(purorderDetail.getTaxAmount());
				}
			}
		}
		
		//含税
		saleOrder.setTotalMoney(purOrder.getTotalMoney());
		//无税总金额
		saleOrder.setNotaxMoney(purOrder.getNotaxMoney());

		return saleOrder;
	}

	/**
	 * @Title:
	 * @param:@param saleOrder
	 * @param:@return
	 * @return:
	 * @Description: 计算：本期已申请通过量/本期已申请待买方确认数量/本期已申请待卖方确认数量
	 * @author:liuhao7@yonyou.com
	 * @date:2019年10月30日
	 */
	private Map<String, Object> getNumBySaleOrderParms(SaleOrderDetail saleOrderDetail, SaleOrder saleOrder) {

		// 结果参数
		Map<String, Object> mapFinal = new HashMap<String, Object>();
		// sql参数
		Map<String, Object> mapResult = new HashMap<String, Object>();

		SaleOrderDetailCriteria criteria = new SaleOrderDetailCriteria();

		// criteria属性值获取
		Long purEnterpriseId = saleOrder.getPurEnterpriseId();
		Long enterpriseId = saleOrder.getEnterpriseId();
		String orderStatus = saleOrder.getOrderStatus();
		String changeStatus = saleOrder.getChangeStatus();
		Date entryIntoForceTime = saleOrder.getEntryIntoForceTime();

		String materialCode = saleOrderDetail.getMaterialCode();
		String recvstorCode = saleOrderDetail.getRecvstorCode();

		// 数据封装
		Map<String, Object> mapFin = new HashMap<String, Object>();

		// 固定参数
		// purenterpriseId
		mapFin.put("purenterpriseid", purEnterpriseId);

		// supenterpriseid
		List listPurEnterpriseid = new ArrayList<>();
		listPurEnterpriseid.add(enterpriseId);
		mapFin.put("supenterpriseid", listPurEnterpriseid);

		// changeStatus
		List listChangeStatus = new ArrayList<>();
		listChangeStatus.add(changeStatus);
		mapFin.put("changeStatus", listChangeStatus);

		// mdcodeList
		List listMdcodeList = new ArrayList<>();
		listMdcodeList.add(recvstorCode);
		mapFin.put("mdcodeList", listMdcodeList);

		// materialList
		List listMaterialList = new ArrayList<>();
		listMaterialList.add(materialCode);
		mapFin.put("materialList", listMaterialList);

		// entryIntoForceTime
		mapFin.put("entryIntoForceTime", entryIntoForceTime);

		criteria.addExtField("extFields", mapFin);

		// 第一个
		List listOrderStatuses = new ArrayList<>();
		// 本期已申请通过量(currentApprovedNum):
		// 其他条件：销售的状态=“买方已审批（10）&不在变更中”且销售订单生效时间>看板更新时间的订单行采购数量总和
		listOrderStatuses.add(10);
		mapFin.put("orderStatuses", listOrderStatuses);
		// 结果集
		List<SaleOrderDetail> listCurrentApprovedNum = iVmiReqSaleOrderNumService.queryVmiSaleorder(criteria);

		if (listCurrentApprovedNum.size() > 0) {
			BigDecimal num = new BigDecimal("0");
			for (SaleOrderDetail one : listCurrentApprovedNum) {
				BigDecimal oneNum = one.getQuantity();
				if (oneNum != null) {
					num = num.add(oneNum);
				}
			}
			mapResult.put("currentApprovedNum", num);
		} else {
			mapResult.put("currentApprovedNum", 0);
		}

		// 第二个
		List listOrderStatusesTwo = new ArrayList<>();
		// 本期已申请待买方确认数量(currentByBuyNum):其他条件：销售订单状态=“待买方确认2/买方审批中9”
		listOrderStatusesTwo.add(2);
		listOrderStatusesTwo.add(9);
		mapFin.replace("orderStatuses", listOrderStatusesTwo);
		List<SaleOrderDetail> listCurrentByBuyNum = iVmiReqSaleOrderNumService.queryVmiSaleorder(criteria);

		if (listCurrentByBuyNum.size() > 0) {
			BigDecimal num = new BigDecimal("0");
			for (SaleOrderDetail one : listCurrentByBuyNum) {
				BigDecimal oneNum = one.getQuantity();
				if (oneNum != null) {
					num = num.add(oneNum);
				}
			}
			mapResult.put("currentByBuyNum", num);
		} else {
			mapResult.put("currentByBuyNum", 0);
		}

		// 第三个
		List listOrderStatusesThree = new ArrayList<>();
		// 本期已申请待卖方确认量(currentBySellNum):其他条件：销售订单状态=“待发布（23）/待卖方确认（12）/买方已拒绝（24）/买方已审批（10）(待卖方确认变更或买方关闭待我确认)
		listOrderStatusesThree.add(12);
		listOrderStatusesThree.add(23);
		listOrderStatusesThree.add(24);
		listOrderStatusesThree.add(10);
		mapFin.replace("orderStatuses", listOrderStatusesThree);

		List<SaleOrderDetail> listCurrentBySellNum = iVmiReqSaleOrderNumService.queryVmiSaleorder(criteria);

		if (listCurrentBySellNum.size() > 0) {
			BigDecimal num = new BigDecimal("0");
			for (SaleOrderDetail one : listCurrentBySellNum) {
				BigDecimal oneNum = one.getQuantity();
				if (oneNum != null) {
					num = num.add(oneNum);
				}
			}
			mapResult.put("currentBySellNum", num);
		} else {
			mapResult.put("currentBySellNum", 0);
		}

		return mapResult;
	}

	/**
	* @Title: 
	* @param:@param saleOrderFromDb
	* @param:@param saleOrderDetail1
	* @param:@return
	* @return: 
	* @Description:   本次可申请补货剩余量(暂时不用)
	* @author:liuhao7@yonyou.com
	* @date:2019年11月10日
	 */
	private BigDecimal getCurrentAskSurplusNum(SaleOrder saleOrderFromDb, SaleOrderDetail saleOrderDetail1) {

		Map<String, Object> mapFinal = new HashMap();

		// 1.1 使用config中的一种配置：计算 “本期可申请补货剩余量”用来校验；

		// 规则：
		// a) 按照客户+物料+供应商+库存组织+仓库维度找到唯一一条VMI看板补货数据，取出补货上限、在途量
		// b) 重新计算对应的“本期已申请通过量”
		// c) 重新计算对应的“本期已申请待买方确认数量”
		// d) 重新计算对应的“本期已申请待卖方确认数量”

		// 最终得到“本期可申请补货剩余量”公式
		// 重新计算除开本张订单外，本期可申请补货剩余量=补货上限-在途量-本期已申请通过量-本期已申请待买方确认数量-本期已申请待卖方确认数量+本订单旧数量

		// 前端传递的子表
		SaleOrderDetail one = saleOrderDetail1;

		// 取出来的数量各个数据
		BoardEntity boardEntity = new BoardEntity();
		// 客户
		boardEntity.setEnterpriseId(saleOrderFromDb.getPurEnterpriseId());
		// 物料;如果前端不给传递，自己再查一遍；
		String productId = one.getProductIid();

		if (!"".equals(productId) && productId != null) {
			boardEntity.setMaterialId(Long.valueOf(productId));
		} else {
			for (int k = 0; k < saleOrderFromDb.getDetailEntityList().size(); k++) {
				SaleOrderDetail saleOrderDetail = saleOrderFromDb.getDetailEntityList().get(k);
				if (one.getId().equals(saleOrderDetail.getId())) {
					productId = saleOrderDetail.getProductIid();
					boardEntity.setMaterialId(Long.valueOf(productId));

				}
			}
		}

		// 供应商id；saleOrder：supplierid；saleOrderDetail:无；
		boardEntity.setSupDocId(saleOrderFromDb.getSupplierid());

		// 库存组织id(库存组织 = saleOrderDetail中的收货组织recv_org)
		Long recvOrg = one.getRecv_org();
		if (!"".equals(recvOrg) && recvOrg != null) {
			boardEntity.setStoreOrgId(recvOrg);
		} else {
			for (int k = 0; k < saleOrderFromDb.getDetailEntityList().size(); k++) {
				SaleOrderDetail saleOrderDetail = saleOrderFromDb.getDetailEntityList().get(k);
				if (one.getId().equals(saleOrderDetail.getId())) {
					recvOrg = saleOrderDetail.getRecv_org();
					boardEntity.setStoreOrgId(recvOrg);
				}
			}
		}
		// 库存组织
		boardEntity.setStoreOrgId(recvOrg);
		
		// 仓库
		//增加粒度判断：库存点粒度0 收货组织+仓库 1 收货组织
		Config[] configs = iVmiConfigService.queryByEnterpriseId(saleOrderFromDb.getPurEnterpriseId());
		String stockdimension = configs[0].getStockdimension();
		if(!"".equals(stockdimension) && stockdimension!=null){
			if("0".equals(stockdimension)){
				String whCode = one.getRecvstorCode();
				if (!"".equals(whCode) && whCode != null) {
					boardEntity.setWhCode(whCode);
				} else {
					for (int k = 0; k < saleOrderFromDb.getDetailEntityList().size(); k++) {
						SaleOrderDetail saleOrderDetail = saleOrderFromDb.getDetailEntityList().get(k);
						if (one.getId().equals(saleOrderDetail.getId())) {
							whCode = saleOrderDetail.getRecvstorCode();
							boardEntity.setWhCode(whCode);
						}
					}
				}
			}
		}
		
		logger.error("按照客户+物料+供应商+库存组织+仓库维度找到唯一一条VMI看板补货数据，取出补货上限、在途量--客户为："+saleOrderFromDb.getPurEnterpriseId()+
				"；物料为："+boardEntity.getMaterialId()+"；供应商为："+saleOrderFromDb.getSupplierid()+"；库存组织："+boardEntity.getStoreOrgId()+"；仓库："+boardEntity.getWhCode());

		// 得到唯一数据；进行数据计算；
		List <BoardEntity>boardEntityList = boardDao.getFillUpUpperNumAndIntransitNum(boardEntity);
		
		if(boardEntityList!=null && boardEntityList.size() == 1){
			if (boardEntity != null) {
				// 补货上限
				BigDecimal decimalFillUpUpperNum = boardEntity.getFillUpUpperNum();

				// 在途量:实时计算；不能从库里取
				BigDecimal decimalIntransitNum = boardEntity.getIntransitNum();
				// 生成各个数量
				Map<String, Object> mapNum = this.getNumBySaleOrderParms(one, saleOrderFromDb);

				// 本期已申请通过量(currentApprovedNum)
				BigDecimal decimalOne = (BigDecimal)mapNum.get("currentApprovedNum");
				// 本期已申请待买方确认数量(currentByBuyNum)
				BigDecimal decimalTwo = (BigDecimal)mapNum.get("currentByBuyNum");
				// 本期已申请待卖方确认数量(currentBySellNum)
				BigDecimal decimalThree = (BigDecimal)mapNum.get("currentBySellNum");
				
				// 本订单旧数量--用id查询出来上一次的数量---本次申请数量
				BigDecimal decimalFour = new BigDecimal("0");
				for (int k = 0; k < saleOrderFromDb.getDetailEntityList().size(); k++) {
					SaleOrderDetail saleOrderDetail = saleOrderFromDb.getDetailEntityList().get(k);
					if (one.getId().equals(saleOrderDetail.getId())) {
						decimalFour = saleOrderDetail.getQuantity();
					}
				}

				BigDecimal decimalApplyRepquantity = decimalFillUpUpperNum.subtract(decimalIntransitNum)
						.subtract(decimalOne).subtract(decimalTwo).subtract(decimalThree).add(decimalFour);
				
				return decimalApplyRepquantity;
			} 
		}
		logger.error("按照客户+物料+供应商+库存组织+仓库维度找到 "+boardEntityList.size()+"条数据");
		return  new BigDecimal("0");
	}

	@Override
	public JSONObject getTranTypeList(PurOrder purOrder) {// 交易类型集合

		JSONObject result = new JSONObject();

		PurOrderCriteria criteria = new PurOrderCriteria();
		criteria.setId(purOrder.getId());
		List<PurOrder> purOrderList = purOrderService.selectPurOrderByPurOrderCriteria(criteria);
		if (purOrderList != null && purOrderList.size() > 0) {
			purOrder = purOrderList.get(0);
			// ycDemo 上存储的业务类型为 1：普通；2：VMI；
			String businessType = String.valueOf(purOrder.getBusinessType());
			List<TransTypeVO> listTrans = iTransTypeVOService.selectByBustypeAndApprove(businessType,
					purOrder.getEnterpriseId());

			String param = JSONObject.toJSONString(listTrans);

			result.put("status", "1");
			result.put("data", param);
			result.put("msg", "获取交易类型集合成功");

		} else {
			result.put("status", 0);
			result.put("msg", "采购订单查询失败");
		}

		return result;
	}

	/**
	 * VMI订单合同取价服务
	 * 匹配逻辑：采购组织+供应商+物料+合同可下单数量
	 * 1.采购组织——一个订单只有单一组织，已经在查询合同时作为条件
	 * 2.供应商——一个订单只有单一供应商，已经在查询合同时作为条件
	 * 3.物料：根据合同是物料合同还是分类合同分别匹配
	 * 4.合同可下单数量不足以覆盖订单行的数量，查询到界面，但不允许界面选择；否则，放到界面，选择选择该合同取价
	 * 
	 */
	@Override
	public JSONObject getContractByPurOrder(PurOrder purOrder) {// 取价专用

		JSONObject result = new JSONObject();
		
		//1.根据界面传入的订单信息，从数据库中查询一次订单；
		//TODO:为什么要从数据库中重新查询一次？如果是界面取价后，没有保存，再次点取价，岂不是原来的取价信息都没有了？
		PurOrderCriteria criteria = new PurOrderCriteria();
		criteria.setId(purOrder.getId());
		purOrder = this.getPurOrder(criteria);

		List<PurOrderDetail> orderDetailList = purOrder.getPurOrderDetailList();
		// 采购表体
		if(orderDetailList==null || orderDetailList.size()==0){
			result.put("status", 0);
			result.put("msg", "采购订单获取表体失败");
			return result;
		}

		//2.根据订单信息查询可用的合同物料行
		//1）合同的供应商与订单相同
		//2）合同的适应组织范围为全部，或者合同适应组织范围包含订单采购组织
		//3）其他条件：合同已生效、租户、合同未失效、物料行在价格有效期内
		//4）物料条件：物料合同使用物料作为条件，分类合同使用分类作为条件
		ContractMaterialCtriteria ctriteria = new ContractMaterialCtriteria();
		// 查询条件供应商封装
		ctriteria.setSupplierid(purOrder.getSupEnterpriseId());
		// 查询条件采购组织封装
		ctriteria.setReqOrgId(purOrder.getOrgId());
		ctriteria.setEnterpriseId(purOrder.getEnterpriseId());
		
		//物料或者物料分类条件：物料合同使用物料作为条件，分类合同使用分类作为条件
		// 物料id集合
		List<String> materialIdlist = new ArrayList<String>();
		List<Long> materialClassIdlist = new ArrayList<Long>();
		for (PurOrderDetail orderDetail : orderDetailList) {
			if(orderDetail.getProductIid()!=null){
				materialIdlist.add(orderDetail.getProductIid());
				materialClassIdlist.add(orderDetail.getMaterialclassId());
			}
		}
		
		// 查询条件货物id封装
		if (materialIdlist != null && materialIdlist.size() > 0) {
			ctriteria.addExtField("materialIdlist", materialIdlist);
		}
		if (materialClassIdlist != null && materialClassIdlist.size() > 0) {
			ctriteria.addExtField("materialClassIdlist", materialClassIdlist);
		}
		
		// 采购组织+供应商+物料”查询对应的有效期内的合同清单；
		logger.error("合同取价条件：ctriteria=【】" + JsonUtils.toJson(ctriteria));
		List<ContractMaterial> ctMList = iAgreement2OrderService.getContractMaterialByCriteriaForVMI(ctriteria);
		/*if (ctMList == null || ctMList.size() == 0) {
			result.put("status", 1);
			result.put("msg", "没有任何匹配的合同！");
			return result;
		}*/
		
		//3.查询合同信息
		List<Long> ctIds = new ArrayList<Long>();
		if (ctMList != null && ctMList.size() > 0) {
			for (ContractMaterial contractMaterial : ctMList) {
				ctIds.add(contractMaterial.getContractId());
			}
		}
		
		// 合同主表信息
		Map<Long,Contract> id2ContractMap = new HashMap<Long,Contract>();
		if (ctIds != null && ctIds.size() != 0) {
			List<Contract> contractList = contractService.batchSelectContractById(ctIds);
			if(contractList!=null && contractList.size()>0){
				for(Contract contract : contractList){
					id2ContractMap.put(contract.getId(), contract);
				}
			}
		}
		
		//付款协议：1 通过合同ids查询所有的付款协议，并封装map
		Map<Long,List<ContractPayTerm>> contractPayTermMap = new HashMap<Long,List<ContractPayTerm>>();
		if (ctIds != null && ctIds.size() != 0) {
			contractPayTermMap = getMapFromContractIds(ctIds);
		}
		
		//4.匹配合同物料行，返回前台
		// 返回结果
		List<PurOrderContractCriteria> orderB2CtmsList = new ArrayList<PurOrderContractCriteria>();
		//循环处理每个订单行，得到该行可取价（能匹配上）的合同行【包括数量不满足的，数量不满足的打上标识，不允许界面选择】	
		for (PurOrderDetail orderDetail : orderDetailList) {
			// 每个物料行的合同子集
			List<PurOrderContractCriteriaDetail> ctmslist = new ArrayList<PurOrderContractCriteriaDetail>();

			Long materialId = orderDetail.getProductIid()==null ? null : Long.parseLong(orderDetail.getProductIid());
			Long materialClassId = orderDetail.getMaterialclassId();
			//循环处理每个合同行，如果物料（物料合同）或者物料分类（分类合同）能够跟订单匹配，即可匹配上
			
			if(ctMList!=null && ctMList.size()>0){
				for (ContractMaterial contractMaterial : ctMList) {
					Long contractId = contractMaterial.getContractId();
					Contract contract = id2ContractMap.get(contractId);
					
					//VMI如果合同没有的话，需要给前端
					if(contract!=null){
						boolean isMatched = false;
						String isMaterialClass = (contract.getIsMaterialClass()==null||"".equals(contract.getIsMaterialClass().trim())) 
			                		? "0" : contract.getIsMaterialClass();
			            if("0".equals(isMaterialClass)){
			            	//物料合同,并且订单上的物料和合同上的物料相同
			            	if(materialId !=null && materialId.equals(contractMaterial.getMaterialId())){
			            		isMatched = true;
			            	}
			            }else if("1".equals(isMaterialClass) && contractMaterial.getMaterialClassId()!=null){
			            	if(materialClassId != null && materialClassId.equals(contractMaterial.getMaterialClassId())){
			            		isMatched = true;
			            	}
		                }else{
		                	continue;
		                }
			            //没有匹配上，继续循环下一行
			            if(!isMatched){
			            	continue;
			            }
			            //付款协议：2 通过map得到改合同对应的付款协议集合
			            List<ContractPayTerm> listPayTerm = contractPayTermMap.get(contract.getId());
			            
			            //匹配上了，构建界面数据+付款协议赋值
						try {
							PurOrderContractCriteriaDetail orderCtmDetail = contructOrderCtmDetail(
									orderDetail, contractMaterial, contract,listPayTerm);
							
							//业务4.包装返回
							//只有可超量或者有剩余可执行量的合同才返回界面
							if(orderCtmDetail.getRemainQuantity()==null || orderCtmDetail.getRemainQuantity().compareTo(new BigDecimal(0))>0){
								ctmslist.add(orderCtmDetail);
							}
						} catch (Exception e) {
							logger.error(e.getMessage());
							result.put("msg", e.getMessage());
							result.put("status", 0);
						}
					}
					
					
				}
			}
			// 接口返回实体封装
			PurOrderContractCriteria purOrderContractCriteria = new PurOrderContractCriteria();
			//对应的订单行id
			purOrderContractCriteria.setOrderDetailId(orderDetail.getId());
			// 商品名称
			purOrderContractCriteria.setProductName(orderDetail.getProductName());
			// 物料规格
			purOrderContractCriteria.setProductSpec(orderDetail.getProductSpec());
			// 物料型号
			purOrderContractCriteria.setProductModel( orderDetail.getProductModel());
			// 数量
			purOrderContractCriteria.setQuantity(orderDetail.getQuantity());
			// 货物单位
			purOrderContractCriteria.setUnit(orderDetail.getUnit());
			// 备注
			purOrderContractCriteria.setMemo(orderDetail.getMemo());
			// 收货组织id
			purOrderContractCriteria.setReceiveOrgId(orderDetail.getReceiveOrgId());
			// 收货组织名称
			purOrderContractCriteria.setReceiveOrgName( orderDetail.getReceiveOrgName());
			// 收货组织编码
			purOrderContractCriteria.setRecOrgCode(orderDetail.getRecOrgCode());
			// 无税单价
			purOrderContractCriteria.setPrice(orderDetail.getPrice());
			// 含税单价
			purOrderContractCriteria.setTaxPrice( orderDetail.getTaxPrice());
			// 税率
			purOrderContractCriteria.setTaxrate(orderDetail.getTaxrate());
			// 计划到货时间
			purOrderContractCriteria.setPlanDeliverDate(orderDetail.getPlanDeliverDate());
			// 品牌
			purOrderContractCriteria.setMaterialBrand(orderDetail.getMaterialBrand());
			// 物料id
			purOrderContractCriteria.setMaterialId(orderDetail.getProductIid());
			// 子表
			purOrderContractCriteria.setListContracts(ctmslist);
			
			orderB2CtmsList.add(purOrderContractCriteria);
		}

		// 返回结果
		result.put("status", "1");
		result.put("msg", "取价列表查询成功");
		result.put("data", orderB2CtmsList);
		logger.error("取价结果:{}" + JsonUtils.toJson(orderB2CtmsList));

		return result;
	}

	private PurOrderContractCriteriaDetail contructOrderCtmDetail(
			PurOrderDetail orderDetail, ContractMaterial contractMaterial,
			Contract contract, List<ContractPayTerm> listPayTerm) {
		// 前端dto
		PurOrderContractCriteriaDetail orderCtmDetail = new PurOrderContractCriteriaDetail();
		// 转换
		// BeanUtils.copyProperties(a,contractMaterial);
		//业务1.属性赋值
		// 合同编码
		orderCtmDetail.setBillno(contract.getBillno());
		/** 采购组织id **/
		orderCtmDetail.setOrgId(contract.getOrgId());
		/** 采购组织名称 **/
		orderCtmDetail.setOrgName(contract.getOrgName());
		// 结算方式
		orderCtmDetail.setSettleType(contract.getSettleType());
		// 价格类型) 1实价 2 浮动金额 3 浮动比例
		orderCtmDetail.setOfferType(contract.getOfferType());
		/** 无税单价 **/
		orderCtmDetail.setPrice(contractMaterial.getPrice());
		/** 含税单价 **/
		orderCtmDetail.setTaxPrice(contractMaterial.getTaxPrice());
		/** 合同数量 **/
		orderCtmDetail.setNum(contractMaterial.getNum());
		/** 单位 **/
		orderCtmDetail.setUnit(contractMaterial.getUnit());
		/** 税率 **/
		orderCtmDetail.setTaxrate(contractMaterial.getTaxrate());
		// 分类合同
		orderCtmDetail.setIsMaterialClass(contract.getIsMaterialClass());
		// 项目名称
		orderCtmDetail.setProjectName(contract.getProjectName());
		// 物料id
		//orderCtmDetail.setMaterialId(contractMaterial.getMaterialId());
		//这里如果是分类合同，物料会是空，导致前端在用物料id匹配时，分类合同无法匹配上，所以直接放订单上的物料即可
		orderCtmDetail.setMaterialId(orderDetail.getProductIid()==null ? null : Long.parseLong(orderDetail.getProductIid()));
		// 合同主表id
		orderCtmDetail.setContractId(contract.getId());
		// 合同子表id
		orderCtmDetail.setContractMaterialId(contractMaterial.getId());
		//计量单位相关的信息，廖芳结论：先跟单位一直，换算率直接赋值1
		orderCtmDetail.setAssUnit(contractMaterial.getUnit());
		orderCtmDetail.setExchangeRate(new BigDecimal(1));
		orderCtmDetail.setAssPrice(contractMaterial.getTaxPrice());
		orderCtmDetail.setNoTaxAssPrice(contractMaterial.getPrice());
		//品牌id
		orderCtmDetail.setRequireBrandId(contractMaterial.getBrandId());
		//品牌code
		orderCtmDetail.setRequireBrandCode(contractMaterial.getBrandCode());
		//品牌name
		orderCtmDetail.setRequireBrand(contractMaterial.getBrand());
		//品牌ErpId
		orderCtmDetail.setRequireBrandErpId(contractMaterial.getBrandErpId());
		
		//处理一步付款协议：将所有的付款协议的id都设置成空,传到前端界面再返回后台时候能区分页面上已有的付款协议，哪些是新增的，哪些是原先的
		for(ContractPayTerm contractPayTerm :listPayTerm){
			contractPayTerm.setId(null);
			contractPayTerm.setMemo(null);
			contractPayTerm.setPayTaxMoney(BigDecimal.ZERO);
		}
		
		//业务2.如果匹配到合同
		//计算得到可执行数量
		Map <Boolean, BigDecimal> map = this.setAccQuantity(contract, contractMaterial, null);
		if(map.containsKey(true)){
			orderCtmDetail.setIfCheck(true);
			//未执行量,无数量控制的合同，可执行量设置为null，前端显示成“--”
			orderCtmDetail.setRemainQuantity(null);
			//付款协议：3给对应的前端对象赋值付款协议子集
			if(listPayTerm!=null && listPayTerm.size()>0){
				orderCtmDetail.setListContractPayTerm(listPayTerm);
			}
		}else{
			BigDecimal enableExecNum = map.get(false);
			//未执行量
			orderCtmDetail.setRemainQuantity(enableExecNum);
			
			//业务3.前端引用字段赋值（用来把执行数量小于申请数量的合同号不可选）
			BigDecimal purOrderDetailQuantity = orderDetail.getQuantity();
			if(enableExecNum!=null && purOrderDetailQuantity!=null ){
				if(enableExecNum.compareTo(purOrderDetailQuantity)>=0){
					orderCtmDetail.setIfCheck(true);
					//付款协议：3给对应的前端对象赋值付款协议子集
					if(listPayTerm!=null && listPayTerm.size()>0){
						orderCtmDetail.setListContractPayTerm(listPayTerm);
					}
				}else{
					orderCtmDetail.setIfCheck(false);
					//付款协议：3给对应的前端对象赋值付款协议子集
					orderCtmDetail.setListContractPayTerm(new ArrayList());
				}
			}
		}
		return orderCtmDetail;
	}

	/**
	 * @Title: 功能1：分析合同的类型如果满足条件，对完成量进行赋值；功能2：不满足条件，返回执行量；
	 * @param:@param ct
	 * @param:@param cm
	 * @param:@param inputQuantity
	 * @param:@return 可执行量；
	 * @return:
	 * @Description:
	 * @author:liuhao7@yonyou.com
	 * @date:2019年11月15日
	 */
	private Map<Boolean, BigDecimal> setAccQuantity(Contract ct, ContractMaterial cm, BigDecimal inputQuantity) {

		BigDecimal notExeNum = new BigDecimal(0);

		Map<Boolean, BigDecimal> map = new HashMap<Boolean, BigDecimal>();

		if (!StringUtils.isBlank(ct.getControlType()) && (ct.getControlType().equals(EControlType.NONE.getCode())
				|| ct.getControlType().equals(EControlType.PRICE.getCode()))) {
			// 1.存在单价合同和无单价无数量合同，满足
			map.put(true, new BigDecimal("0"));
		} else {
			// 该合同是数量合同或者单价数量合同，或者ControlType为空（兼容历史数据）
			if (ct.getExecType() != null && ct.getExecType().equals(EExecType.NUM_OVER.getCode())) {// 可超量执行
				if (cm.getOverExecRate() == null) {
					// 3.2如果存在单价数量合同和数量合同，并且合同可超数量执行，并且超量比例为空，满足；匹配成功，放到返回Map中
					map.put(true, new BigDecimal("0"));
				} else {
					// 3.3上面两种合同都不存在时（全是数量合同或单价数量合同，并且：不可超量执行、或者可超量执行但超量比例非空），逻辑如下：所有合同的未执行数量之和大于需求数量，其中未执行数量计算逻辑如下
					// 3.3.2如果合同可超量执行，但超量比例非空，未执行数量 = 合同数量*（1+超量比例/100） - 已执行数量；
					notExeNum = notExeNum.add((cm.getNum() == null ? new BigDecimal(0) : cm.getNum())
							.multiply((cm.getOverExecRate().divide(new BigDecimal(100)).add(new BigDecimal(1))))
							.subtract(cm.getAccomplishedQuantity() == null ? new BigDecimal(0)
									: cm.getAccomplishedQuantity()));
					map.put(false, notExeNum);
				}
			} else {// 不可超量执行
				// ct.getExecType().equals(EExecType.NUM.getCode())
				// 3.3上面两种合同都不存在时（全是数量合同或单价数量合同，并且：不可超量执行、或者可超量执行但超量比例非空），逻辑如下：所有合同的未执行数量之和大于需求数量，其中未执行数量计算逻辑如下
				// 3.3.1如果合同不可超量执行，未执行数量 = 合同数量 - 已执行数量；
				notExeNum = notExeNum.add((cm.getNum() == null ? new BigDecimal(0) : cm.getNum()).subtract(
						cm.getAccomplishedQuantity() == null ? new BigDecimal(0) : cm.getAccomplishedQuantity()));
				map.put(false, notExeNum);
			}
		}
		return map;
	}

	/**
	 * @Title:
	 * @param:@param accomplishedQuantity（已完成数量）
	 * @param:@param inputQuantity（现在申请的数量）
	 * @param:@param contractMaterial（合同物料行实体）
	 * @return:
	 * @Description: 取价成功之后，回写合同的已完成
	 * @author:liuhao7@yonyou.com
	 * @date:2019年11月8日
	 */
	private void setAccomplishedQuantity(BigDecimal inputQuantity, ContractMaterial contractMaterial) {
		// 可用--进行数量相加
		List<ContractMaterial> updateList = new ArrayList();

		contractMaterial.setInputQuantity(inputQuantity);

		if (contractMaterial.getInputQuantity() != null && contractMaterial.getAccomplishedQuantity() != null) {
			contractMaterial.setAccomplishedQuantity(
					contractMaterial.getInputQuantity().add(contractMaterial.getAccomplishedQuantity()));
			updateList.add(contractMaterial);
			contractMaterialService.updateContractMaterial(updateList);
		}
	}
	
	/**
	* @Title: 
	* @param:@param purOrderDetail
	* @param:@param cm
	* @param:@return
	* @return: 
	* @Description:   根据合同物料采购订单子表、销售订单子表赋值
	* @author:liuhao7@yonyou.com
	* @date:2019年11月15日
	 */
	private Map<String, Object> setPurPropertysFromContractMaterial(SaleOrderDetail saleOrderDetail,
			PurOrderDetail purOrderDetail, Contract contract, ContractMaterial contractMaterialOne) {
		
		// 合同编号
		List<Long> ids = new ArrayList<Long>();
		ids.add(contractMaterialOne.getContractId());
		if (ids != null && ids.size() > 0) {
			List<Contract> list = contractService.batchSelectContractById(ids);
			if (list != null && list.size() > 0) {
				contract = list.get(0);
				purOrderDetail.setContractBillno(contract.getBillno());
			}
		}
		
		//品牌信息
		String requireBrand = contractMaterialOne.getBrand();
		Long requireBrandId = contractMaterialOne.getBrandId();
		String requireBrandCode = contractMaterialOne.getBrandCode();
		String requireBrandErpId = contractMaterialOne.getBrandErpId();

		// 采购订单子表字段回写----------
		// 含税单价：taxPrice
		purOrderDetail.setTaxPrice(contractMaterialOne.getTaxPrice());
		// 无税单价：price;
		purOrderDetail.setPrice(contractMaterialOne.getPrice());
		//确认无税单价(此时取合同的单价)
		purOrderDetail.setConPrice(contractMaterialOne.getPrice());
		//确认含税单价(此时取合同的含税单价)
		purOrderDetail.setConTaxPrice(contractMaterialOne.getTaxPrice());
		// 税率
		purOrderDetail.setTaxrate(contractMaterialOne.getTaxrate());
		// 品牌字段：
		purOrderDetail.setRequireBrand(requireBrand);
		purOrderDetail.setRequireBrandId(requireBrandId);
		purOrderDetail.setRequireBrandCode(requireBrandCode);
		purOrderDetail.setRequireBrandErpId(requireBrandErpId);
		// 协议直采合同id 赋值
		purOrderDetail.setContractId(contractMaterialOne.getContractId());// 主
		purOrderDetail.setContractMaterialId(contractMaterialOne.getId());// 子
		//合同单号
		purOrderDetail.setContractBillno(contract.getBillno());
		// 销售子表字段回写----------
		// 含税单价：taxPrice;
		saleOrderDetail.setTaxPrice(contractMaterialOne.getTaxPrice());
		// 无税单价：price;
		saleOrderDetail.setPrice(contractMaterialOne.getPrice());
		//确认无税单价(此时取合同的单价)
		saleOrderDetail.setConPrice(contractMaterialOne.getPrice());
		//确认含税单价(此时取合同的含税单价)
		saleOrderDetail.setConTaxPrice(contractMaterialOne.getTaxPrice());
		// 税率
		saleOrderDetail.setTaxrate(contractMaterialOne.getTaxrate());
		// 需求单id
		saleOrderDetail.setPritemId(contractMaterialOne.getPritemid());
		// 品牌字段：
		saleOrderDetail.setRequireBrand(requireBrand);
		saleOrderDetail.setRequireBrandId(requireBrandId);
		saleOrderDetail.setRequireBrandCode(requireBrandCode);
		saleOrderDetail.setRequireBrandErpId(requireBrandErpId);

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("saleOrderDetail", saleOrderDetail);
		map.put("purOrderDetail", purOrderDetail);
		return map;
	}
	/**
	* @Title: 
	* @param:@param contractMaterial
	* @param:@param contract
	* @param:@param saleOrderDetail
	* @param:@return
	* @return: 
	* @Description:   可执行数量的回写（修改已完成量）
	* @author:liuhao7@yonyou.com
	* @date:2019年11月15日
	 */
	private Boolean updateAccomplishedQuantity (ContractMaterial contractMaterial,Contract contract ,PurOrderDetail purOrderDetail){
		Boolean flag = false;
		// 输入数量
		BigDecimal inputQuantity = (purOrderDetail.getQuantity() == null ? new BigDecimal("0")
				: purOrderDetail.getQuantity());
		// 1.是否允许回写数量；2.如果不允许则判断可执行数量是否覆盖输入数量；
		Map<Boolean, BigDecimal> resultMap = this.setAccQuantity(contract, contractMaterial,
				inputQuantity);

		if(resultMap.containsKey(true)){
			// 已完成量回写
			this.setAccomplishedQuantity(inputQuantity, contractMaterial);
			flag = true;
		}else{
			if(resultMap.get(false).compareTo(inputQuantity) >= 0){
				// 已完成量回写
				this.setAccomplishedQuantity(inputQuantity, contractMaterial);
				flag = true;
			}
		}
		return flag ;
	}

	/**
	 *暂时不用
	 */
	@Override
	public BigDecimal getThisAskfillNum(SaleOrder saleOrder,SaleOrderDetail	saleOrderDetail) {
		BigDecimal result= this.getCurrentAskSurplusNum(saleOrder, saleOrderDetail);
		return result;
	}
	/**
	* @Title: 
	* @param:@param purorder
	* @param:@return
	* @return: 
	* @Description:   付款协议转换销售订单
	* @author:liuhao7@yonyou.com
	* @date:2019年12月2日
	 */
	private Map<String,List> getSaleOrderPayTerm(List <ContractPayTerm> list,PurOrder purOrder,SaleOrder saleOrder){
		
		List<SaleOrderPayTerm> listSaleOrderPayTerm = new ArrayList<SaleOrderPayTerm>();
		
		List<PurOrderPayTerm> listPurOrderPayTerm = new ArrayList<PurOrderPayTerm>();
		
		Map <String,List> map = new HashMap<String,List>();
		
		if(list!=null && list.size()>0){
			for(ContractPayTerm contractPT:list){
				
				SaleOrderPayTerm saleOrderPayTerm =new SaleOrderPayTerm ();
				PurOrderPayTerm purOrderPayTerm =new PurOrderPayTerm ();
				
				//赋值
				
				//外系统付款协议子表id
				//saleOrderPayTerm.setPayTermDetailId(contractPT.getpayTermDetailId);
				//purOrderPayTerm.setPayTermDetailId(payTermDetailId);
				//上游付款协议子表id（合同、定标单）
				//saleOrderPayTerm.setSourcePayTermId(sourcePayTermId);
				//purOrderPayTerm.setSourcePayTermId(sourcePayTermId);
				//采购订单 付款协议子表id
				//saleOrderPayTerm.setPurOrderPayTermId(purOrderPayTermId);
				//purOrderPayTerm.setSaleOrderPayTermId(saleOrderPayTermId);
				
				//所属租户id(在order的保存服务中统一处理)
				//saleOrderPayTerm.setEnterpriseId(contractPT.getEnterpriseId());
				//purOrderPayTerm.setEnterpriseId(contractPT.getEnterpriseId());
				//所属租户名称
				//saleOrderPayTerm.setEnterpriseName(contractPT.getEnterpriseName());
				//purOrderPayTerm.setEnterpriseName(contractPT.getEnterpriseName());
				//主表id
				//saleOrderPayTerm.setSaleOrderId(saleOrder.getId());
				//purOrderPayTerm.setPurOrderId(purOrder.getId());
				
				//付款阶段
				saleOrderPayTerm.setPayPeriod(contractPT.getPayPeriod());
				purOrderPayTerm.setPayPeriod(contractPT.getPayPeriod());
				//付款起点
				saleOrderPayTerm.setPayPoint(contractPT.getPayPoint());
				purOrderPayTerm.setPayPoint(contractPT.getPayPoint());
				
				saleOrderPayTerm.setPayPointName(contractPT.getPayPointName());
				purOrderPayTerm.setPayPointName(contractPT.getPayPointName());
				//付款起点延期天数
				saleOrderPayTerm.setPayPointAfterDay(contractPT.getPayPointAfterDay());
				purOrderPayTerm.setPayPointAfterDay(contractPT.getPayPointAfterDay());
				//付款比例
				if (contractPT.getPayRatio() != null && purOrder.getTotalMoney() != null) {
					saleOrderPayTerm.setPayRatio(contractPT.getPayRatio());
					purOrderPayTerm.setPayRatio(contractPT.getPayRatio());
					// 付款金额(含税金额、四舍五入两位)
					saleOrderPayTerm.setPayTaxMoney(
							purOrder.getTotalMoney().multiply(contractPT.getPayRatio().divide(new BigDecimal("100")))
									.setScale(2, BigDecimal.ROUND_HALF_UP));
					purOrderPayTerm.setPayTaxMoney(
							purOrder.getTotalMoney().multiply(contractPT.getPayRatio().divide(new BigDecimal("100")))
									.setScale(2, BigDecimal.ROUND_HALF_UP));
				}
				
				//是否预付款
				saleOrderPayTerm.setAdvancePay(contractPT.getAdvancePay());
				purOrderPayTerm.setAdvancePay(contractPT.getAdvancePay());
				//是否质保金
				saleOrderPayTerm.setShelf(contractPT.getShelf());
				purOrderPayTerm.setShelf(contractPT.getShelf());
				//结算方式 
				saleOrderPayTerm.setSettleType(contractPT.getSettleType());
				purOrderPayTerm.setSettleType(contractPT.getSettleType());
				
				saleOrderPayTerm.setSettleTypeName(contractPT.getSettleTypeName());
				purOrderPayTerm.setSettleTypeName(contractPT.getSettleTypeName());
				
				saleOrderPayTerm.setMemo(contractPT.getMemo());
				purOrderPayTerm.setMemo(contractPT.getMemo());
				
				saleOrderPayTerm.setDr(0);
				purOrderPayTerm.setDr(0);
				
				saleOrderPayTerm.setTs(new Date());
				purOrderPayTerm.setTs(new Date());
				// 变更动作：删除原协议、变更协议、新增协议
				
				saleOrderPayTerm.setRowStatus(PayTermChangeEnum.ORIGINALROW.getCode());
				purOrderPayTerm.setRowStatus(PayTermChangeEnum.ORIGINALROW.getCode());
				
				listSaleOrderPayTerm.add(saleOrderPayTerm);
				listPurOrderPayTerm.add(purOrderPayTerm);
			}
			
			map.put("saleOrderList", listSaleOrderPayTerm);
			map.put("purOrderList", listPurOrderPayTerm);
		}
		return map;
	}
	
	/**
	* @Title: 
	* @param:@param listContractIds
	* @param:@return
	* @return: 
	* @Description:   通过合同id集合查询出Map<Long,List<ContractPayTerm>>  的集合
	* @author:liuhao7@yonyou.com
	* @date:2019年12月3日
	 */
	private Map<Long,List<ContractPayTerm>> getMapFromContractIds(List<Long> listContractIds){
		// 5.5付款协议
		Map<Long,List<ContractPayTerm>> contractPayTermMap = new HashMap<Long,List<ContractPayTerm>>();
		if (listContractIds != null && listContractIds.size() != 0) {
			//通过合同的id集合查找出所有的合同
			List<Contract> listContracts = contractService.batchSelectById(listContractIds);
			//存放付款
			if(listContracts!=null && listContracts.size()>0){
				for(Contract contract :listContracts){
					//付款协议；
					if(contract.getContractPayTermList()!=null && contract.getContractPayTermList().size()>0){
						contractPayTermMap.put(contract.getId(), contract.getContractPayTermList());
					}
				}
			}
		}
		return contractPayTermMap;
	}
	
	/**
	* @Title: 
	* @param:@param purOrderPayTermList
	* @param:@param purOrder
	* @param:@return
	* @return: 
	* @Description:   付款协议校验
	* @author:liuhao7@yonyou.com
	* @date:2019年12月4日
	 */
	private ServiceResponse<Integer> checkPayTerm(List<PurOrderPayTerm> purOrderPayTermList, PurOrder purOrder) {
		ServiceResponse<Integer> response = new ServiceResponse<Integer>();
		if (purOrderPayTermList != null && purOrderPayTermList.size() > 0) {
			BigDecimal payRatioSum = BigDecimal.ZERO;
			BigDecimal totalPayTaxMoney = BigDecimal.ZERO;
			boolean hasPayTerm = false;
			for (PurOrderPayTerm purOrderPayTerm : purOrderPayTermList) {
				// 删除的付款协议表体
				if ("del".equals(purOrderPayTerm.getRowStatus())
						|| PayTermChangeEnum.DELETEROW.getCode().equals(purOrderPayTerm.getRowStatus())) {
					continue;
				}
				hasPayTerm = true;
				if (purOrderPayTerm.getPayPeriod() == null) {
					response.setCode("false");
					response.setMsg("订单付款协议阶段不能为空！");
					return response;
				}
				if (purOrderPayTerm.getPayRatio() == null) {
					response.setCode("false");
					response.setMsg("订单付款协议比例不能为空！");
					return response;
				} else {
					payRatioSum = payRatioSum.add(purOrderPayTerm.getPayRatio());
					totalPayTaxMoney = totalPayTaxMoney.add(purOrderPayTerm.getPayTaxMoney());
				}
				if (org.springframework.util.StringUtils.isEmpty(purOrderPayTerm.getPayPoint())) {
					response.setCode("false");
					response.setMsg("订单付款协议起点不能为空！");
					return response;
				}
				if (org.springframework.util.StringUtils.isEmpty(purOrderPayTerm.getPayPointAfterDay())) {
					response.setCode("false");
					response.setMsg("订单付款协议账期天数不能为空！");
					return response;
				}
				if (org.springframework.util.StringUtils.isEmpty(purOrderPayTerm.getSettleType())) {
					response.setCode("false");
					response.setMsg("订单付款协议结算方式不能为空！");
					return response;
				}
			}

			if (hasPayTerm && payRatioSum.compareTo(BigDecimal.valueOf(100)) != 0) {
				response.setCode("false");
				response.setMsg("订单付款协议比例总和不等于100%，请检查！");
				return response;
			}
			if (hasPayTerm && totalPayTaxMoney.compareTo(purOrder.getTotalMoney()) > 0) {
				response.setCode("false");
				response.setMsg("订单付款协议金额不能超过订单总额！");
				return response;
			}
		}

		return response;
	}
	
	/**
	* @Title: 
	* @param:@param purOrder
	* @param:@return
	* @return: 
	* @Description:   重新给付款协议打包封装
	* @author:liuhao7@yonyou.com
	* @date:2019年12月5日
	 */
	private PurOrder getPurOrderPayTermBySelf(PurOrder purOrder) {

		if (purOrder != null && purOrder.getPurOrderPayTermList() != null
				&& purOrder.getPurOrderPayTermList().size() > 0) {
			// 循环赋值
			for (PurOrderPayTerm purOrderPayTerm : purOrder.getPurOrderPayTermList()) {
				//状态得重新赋值
				purOrderPayTerm.setRowStatus(PayTermChangeEnum.ORIGINALROW.getCode());
				//时间得重新赋值
				purOrderPayTerm.setTs(new Date());
			}
		}
		return purOrder;
	}
	
	/**
	* @Title: 
	* @param:@param purOrder
	* @param:@param saleOrder
	* @param:@return
	* @return: 
	* @Description:   根据采购订单付款协议赋值给销售订单
	* @author:liuhao7@yonyou.com
	* @date:2019年12月5日
	 */
	private SaleOrder getSaleOrderPayTermByPurOrder(PurOrder purOrder, SaleOrder saleOrder) {

		if (purOrder != null && purOrder.getPurOrderPayTermList() != null
				&& purOrder.getPurOrderPayTermList().size() > 0) {

			List<SaleOrderPayTerm> saleOrderPayTermList = new ArrayList();
			// 循环赋值
			for (PurOrderPayTerm purOrderPayTerm : purOrder.getPurOrderPayTermList()) {

				SaleOrderPayTerm saleOrderPayTerm = new SaleOrderPayTerm();
				// 付款阶段
				saleOrderPayTerm.setPayPeriod(purOrderPayTerm.getPayPeriod());
				// 付款起点
				saleOrderPayTerm.setPayPoint(purOrderPayTerm.getPayPoint());

				saleOrderPayTerm.setPayPointName(purOrderPayTerm.getPayPointName());
				// 付款起点延期天数
				saleOrderPayTerm.setPayPointAfterDay(purOrderPayTerm.getPayPointAfterDay());
				// 付款比例
				if (purOrderPayTerm.getPayRatio() != null && purOrderPayTerm.getPayTaxMoney() != null) {

					saleOrderPayTerm.setPayRatio(purOrderPayTerm.getPayRatio());
					// 付款金额(含税金额、四舍五入两位)
					saleOrderPayTerm.setPayTaxMoney(purOrderPayTerm.getPayTaxMoney());
				}
				// 是否预付款
				saleOrderPayTerm.setAdvancePay(purOrderPayTerm.getAdvancePay());
				// 是否质保金
				saleOrderPayTerm.setShelf(purOrderPayTerm.getShelf());
				// 结算方式
				saleOrderPayTerm.setSettleType(purOrderPayTerm.getSettleType());

				saleOrderPayTerm.setSettleTypeName(purOrderPayTerm.getSettleTypeName());

				saleOrderPayTerm.setMemo(purOrderPayTerm.getMemo());

				saleOrderPayTerm.setDr(0);

				saleOrderPayTerm.setTs(new Date());
				// 变更动作：删除原协议、变更协议、新增协议
				saleOrderPayTerm.setRowStatus(PayTermChangeEnum.ORIGINALROW.getCode());

				saleOrderPayTermList.add(saleOrderPayTerm);
			}
			saleOrder.setSaleOrderPayTermList(saleOrderPayTermList);
		}
		return saleOrder;
	}
	
	/**
	*test poc branch 
	*/
	private Map <String ,List> getAllCheck (PurOrder purOrder,SaleOrder saleOrder){
		Map map = new HashMap <String ,List>();
		List<PurOrderDetail> list = purOrder.getDetailList();
		List<SaleOrderDetail>listSale = saleOrder.getDetailList();
		if(list !=null && list.size()>0 && listSale !=null && listSale.size()>0{
			for(PurOrderDetail purOrderDetail :list){
				purOrderDetail.setId(null)
			}
		}
	}
}
