<template>
  <table-manage>
    <template #search>
      <el-form-item label="日志编号">
        <el-input placeholder="日志编号" v-model="pageParam.searchObject.logNumber"/>
      </el-form-item>
      <el-form-item label="操作人员">
        <el-input placeholder="操作人员" v-model="pageParam.searchObject.operatorName"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadTableData">搜索</el-button>
        <el-button @click="onReset">重置</el-button>
      </el-form-item>
    </template>

    <template #operate>
      <el-button type="danger" @click="onBatchDelete">批量删除</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="logNumber" label="日志编号"/>
        <el-table-column prop="operatorName" label="操作人"/>
        <el-table-column prop="ipAddress" label="IP地址"/>
        <el-table-column prop="requestWay" label="请求方式"/>
        <el-table-column prop="operationStatus" label="操作状态">
          <template #default="scope">
            <el-tag :type="getOperationStatus(scope.row.operationStatus, 0)">{{getOperationStatus(scope.row.operationStatus, 1)}}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="logType" label="日志类型">
          <template #default="scope">
            <el-tag :type="getLogType(scope.row.logType, 0)">{{getLogType(scope.row.logType, 1)}}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationTime" label="操作时间" width="200"/>
        <el-button size="small" type="danger" plain @click="onDelete(scope.row.id)">删除</el-button>
        <el-table-column fixed="right" label="操作" width="80">
          <template #default="scope">
            <el-button size="small" type="danger" plain @click="onDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #page>
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
                     :total="pageVo.total" :page-size="pageParam.pageSize" :page-sizes=[5,10,15]
                     @current-change="onPageChange" @size-change="onSizeChange"/>
    </template>
  </table-manage>
</template>

<script setup lang="ts">
import {nextTick, onMounted, ref, watch} from "vue";
import SraSimpleTable from "@/components/table/simple-table/SraSimpleTable.vue";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import operationLogApi from "@/api/system/operation-log-api";
import TableManage from "@/components/container/TableManage.vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {deleteBatch} from "@/api/system/user-api";

// 分页参数
const pageParam = ref<PageParam>({pageNo: 1, pageSize: 15, searchObject: {}});
// api返回的分页数据
const pageVo = ref<PageVO>({pageNo: 1, pageSize: 15, total: 0, records: []});
// 加载进度
const loading = ref<boolean>(true);
const getLogType: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'success', text: '登录日志'};
      break;
    case 1:
      obj = {color: 'warning', text: '操作日志'};
      break;
  }
  if (type === 0) {
    return obj.color;
  } else {
    return obj.text;
  }
}
const getOperationStatus: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'danger', text: '异常'};
      break;
    case 1:
      obj = {color: 'success', text: '成功'};
      break;
  }
  if (type === 0) {
    return obj.color;
  } else {
    return obj.text;
  }
}
const multipleSelection = ref<any[]>([]);

// 初始化数据
onMounted(() => {
  loadTableData();
});

const onReset = (): void => {
  pageParam.value.searchObject = {};
}

const onDelete = (id: string) => {
  reqSuccessFeedback(operationLogApi.deleteBatch([id]), '删除成功', () => {
    loadTableData();
  });
}

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  let param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    operationLogVO: pageParam.value.searchObject
  };
  reqCommonFeedback(operationLogApi.listByPage(param), (data: any) => {
    pageVo.value.records = data.rows;
    pageVo.value.total = data.recordCount;
    loading.value = false;
  });
}

const onBatchDelete = () => {
  let ids: string[] = [];
  multipleSelection.value.map((item, index) => ids.push(item.id));
  ElMessageBox.confirm('确认删除所选日志?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    reqCommonFeedback(operationLogApi.deleteBatch(ids), () => {
      ElMessage({type: 'success', message: '删除成功'});
      loadTableData();
    });
  });
}

const onPageChange = (currentPage: number) => {
  pageParam.value.pageNo = currentPage;
  nextTick(() => loadTableData());
}

const onSizeChange = (size: number) => {
  pageParam.value.pageSize = size;
  nextTick(() => loadTableData());
}

const handleSelectionChange = (arr: any) => {
  multipleSelection.value = arr;
}
</script>

<style scoped></style>