<template>
  <table-manage>
    <!-- 表格操作 -->
    <template #search>
      <el-form>
        <el-form-item label="菜单名称">
          <el-input placeholder="菜单名称" v-model:model-value="searchObj.menuName"/>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="loadTableData">搜索</el-button>
      <el-button @click="resetSearchForm">重置</el-button>
      <el-button @click="onExpandAll">
        <el-icon>
          <arrow-right-bold v-if="!isExpandAll"/>
          <arrow-down-bold v-else/>
        </el-icon>
        {{ isExpandAll ? '收起' : '展开' }}
      </el-button>
    </template>

    <template #operate>
      <el-button type="primary" @click="onAdd">添加菜单</el-button>
    </template>

    <!-- 表格视图 -->
    <template #default>
      <el-table v-if="isShowTable" stripe row-key="id" :data="records" v-model:default-expand-all="isExpandAll">
        <el-table-column prop="iconPath" width="100" label="图标">
          <template #default="scope">
            <el-icon>
              <component :is="scope.row.iconPath"></component>
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="menuName" width="200" label="名称"/>
        <el-table-column width="300" prop="routerPath" label="路由地址"/>
        <el-table-column prop="menuType" label="菜单类型">
          <template #default="scope">
            <el-tag :type="getMenuType(scope.row.menuType, 0)">{{ getMenuType(scope.row.menuType, 1) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isExternalLink" label="是否外链">
          <template #default="scope">
            <el-tag :type="getConfirm(scope.row.isExternalLink, 0)">{{ getConfirm(scope.row.isExternalLink, 1) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="menuStatus" label="菜单状态">
          <template #default="scope">
            <el-tag :type="getMenuStatus(scope.row.menuStatus, 0)">{{ getMenuStatus(scope.row.menuStatus, 1) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" width="200" label="创建时间"/>
        <el-table-column prop="updateTime" width="200" label="更新时间"/>
        <!-- 单行操作 -->
        <el-table-column fixed="right" width="150" label="操作">
          <template #default="scope">
            <el-button size="small" @click="onEdit(scope.row)">编辑</el-button>
            <el-button size="small" plain type="danger" @click="onRemove(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <!-- 编辑对话框 -->
    <template #form>
      <el-dialog v-model="dialogFormVisible" :title="`${editForm.id? '编辑' : '添加'}菜单`">
        <el-form ref="sttFormRef" label-width="120px" :model="editForm" :rules="rules">
          <el-form-item prop="menuName" label="菜单名称">
            <el-input v-model="editForm.menuName"></el-input>
          </el-form-item>
          <el-form-item prop="menuType" label="菜单类型">
            <el-radio-group v-model="editForm.menuType" @change="menuTypeChange">
              <el-radio :label="0">目录</el-radio>
              <el-radio :label="1">菜单</el-radio>
              <el-radio :label="2">按钮</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item prop="routerPath" label="路由地址">
            <el-input v-model="editForm.routerPath"></el-input>
          </el-form-item>
          <el-form-item v-if="isShowExternalLink" prop="isExternalLink" label="是否外链">
            <el-radio-group v-model="editForm.isExternalLink">
              <el-radio :label="0">是</el-radio>
              <el-radio :label="1">否</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item prop="sort" label="显示顺序">
            <el-input v-model="editForm.sort" type="number"></el-input>
          </el-form-item>
          <el-form-item label="菜单图标">
            <icon-selection v-model="editForm.iconPath"/>
          </el-form-item>
          <el-form-item label="上级菜单">
            <el-cascader clearable v-model="editForm.parentId" placeholder="选择节点"
                         :props="defaultProps" :options="records" :show-all-levels="false"
                         @change="handleChange">
            </el-cascader>
          </el-form-item>
          <el-form-item prop="menuStatus" label="菜单状态">
            <el-radio-group v-model="editForm.menuStatus">
              <el-radio :label="0">显示</el-radio>
              <el-radio :label="1">隐藏</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <el-button type="primary" @click="doUpdate(sttFormRef)">确认</el-button>
        </span>
        </template>
      </el-dialog>
    </template>
  </table-manage>
</template>

<script setup lang="ts">
import {nextTick, onMounted, reactive, ref} from "vue";
import IconSelection from "@/components/selection/IconSelection.vue";
import {listByTree, add, deleteBatch, update} from "@/api/system/menu-api";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import TableManage from "@/components/container/TableManage.vue";
import {ElForm} from "element-plus/es";
import {ElMessage, ElMessageBox} from "element-plus";

type FormInstance = InstanceType<typeof ElForm>
const sttFormRef = ref<FormInstance>();

// 级联选择框配置
const defaultProps = {
  value: 'id',
  label: 'menuName',
  children: 'children',
  checkStrictly: true
}

const records = ref<any>();
const searchObj = ref<MenuModel>({});
const isExpandAll = ref<boolean>(true);
const getMenuType: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'success', text: '目录'};
      break;
    case 1:
      obj = {color: 'info', text: '菜单'};
      break;
    case 2:
      obj = {color: 'info', text: '按钮'};
      break;
  }
  if (type === 0) {
    return obj.color;
  } else {
    return obj.text;
  }
}
const getConfirm: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'success', text: '是'};
      break;
    case 1:
      obj = {color: 'info', text: '否'};
      break;
  }
  if (type === 0) {
    return obj.color;
  } else {
    return obj.text;
  }
}
const getMenuStatus: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'success', text: '显示'};
      break;
    case 1:
      obj = {color: 'warning', text: '隐藏'};
      break;
  }
  if (type === 0) {
    return obj.color;
  } else {
    return obj.text;
  }
}
// 表单参数
const editForm = ref<MenuModel>({});
// 加载进度
const loading = ref<boolean>(true);
// 表单校验规则
const rules = reactive({
  menuName: [{required: true, min: 2, max: 30, message: '长度限制2~30', trigger: 'blur'}],
  menuType: [{required: true, message: '请选择菜单编号', trigger: 'blur'}],
  menuStatus: [{required: true, message: '请选择菜单状态', trigger: 'blur'}],
  routerPath: [{required: true, min: 2, max: 255, message: '长度限制2~255', trigger: 'blur'}],
  isExternalLink: [{required: true, message: '请选择链接类型', trigger: 'blur'}],
});
// 是否显示外链选择按钮
const isShowExternalLink = ref<boolean>(false);
const dialogFormVisible = ref<boolean>(false);
const isShowTable = ref<boolean>(true);

// 初始化数据
onMounted(() => {
  loadTableData();
});

const onEdit = (row: MenuModel): void => {
  editForm.value = row;
  dialogFormVisible.value = true;
}

const onAdd = () => {
  dialogFormVisible.value = true;
  editForm.value = {menuType: 1, menuStatus: 0};
}

const onRemove = (row: MenuModel): void => {
  ElMessageBox.confirm('确认删除该菜单?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    reqSuccessFeedback(deleteBatch([row.id]), '删除成功', () => {
      loadTableData();
    });
  });
}

const loadTableData = (): void => {
  if (!loading.value) loading.value = true;
  let param = {menuVO: {isMenu: 1, menuName: searchObj.value.menuName}};
  reqCommonFeedback(listByTree(param), (data: any) => {
    records.value = data;
    loading.value = false;
  });
}

const doUpdate = (formEl: any): void => {
  editForm.value.isMenu = 1;
  formEl.validate((valid: any) => {
    if (valid) {
      if (!editForm.value.id) {
        reqSuccessFeedback(add(editForm.value), '新增成功', () => {
          loadTableData();
          dialogFormVisible.value = false;
        });
      } else {
        reqSuccessFeedback(update(editForm.value), '修改成功', () => {
          loadTableData();
          dialogFormVisible.value = false;
        });
      }
    }
  });
}

const handleChange = (data: any) => {
  if (!data) {
    editForm.value.parentId = '0';
    return;
  }
  editForm.value.parentId = data[data.length - 1] ? data[data.length - 1] : '0';
}

const menuTypeChange = (value: string) => {
  isShowExternalLink.value = value === "1";
}

const onExpandAll = () => {
  isShowTable.value = false;
  isExpandAll.value = !isExpandAll.value;
  nextTick(() => {
    isShowTable.value = true;
  });
}

const resetSearchForm = () => {
  searchObj.value.menuName = '';
}
</script>

<style scoped></style>