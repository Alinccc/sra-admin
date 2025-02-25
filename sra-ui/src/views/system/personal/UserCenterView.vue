<template>
  <el-space style="width: 100%" direction="vertical" alignment="stretch">
    <el-card shadow="never">
      <el-descriptions title="个人信息" :column="3" border>
        <el-descriptions-item>
          <template #label>
            <div>
              <el-icon>
                <user/>
              </el-icon>
              账号名
            </div>
          </template>
          {{ detailUser.username }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div>
              <el-icon>
                <user/>
              </el-icon>
              账号角色
            </div>
          </template>
          {{ detailUser.roleName }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">
              <el-icon>
                <tickets/>
              </el-icon>
              用户昵称
            </div>
          </template>
          {{ detailUser.nickname }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">
              <el-icon>
                <tickets/>
              </el-icon>
              性别
            </div>
          </template>
          {{ getSex(detailUser.sex) }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div>
              <el-icon>
                <iphone/>
              </el-icon>
              手机号
            </div>
          </template>
          {{ detailUser.mobilePhone ? detailUser.mobilePhone : '...' }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div>
              <el-icon>
                <location/>
              </el-icon>
              邮箱
            </div>
          </template>
          {{ detailUser.email ? detailUser.email : '...' }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">
              <el-icon>
                <office-building/>
              </el-icon>
              最后登录IP
            </div>
          </template>
          {{ detailUser.lastLoginIp }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">
              <el-icon>
                <office-building/>
              </el-icon>
              最后登录时间
            </div>
          </template>
          {{ detailUser.lastLoginTime }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 用户表单 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>更新资料</span>
        </div>
      </template>

      <el-form ref="ucvFormRef" label-width="120px" label-position="right" :rules="rules" :model="editForm">

        <el-form-item prop="avatar" label="用户头像" :auto-upload="false" list-type="picture-card">
          <el-upload action="/api/file/upload" list-type="picture-card" :limit="1" :on-success="handleAvatarSuccess"
                     :before-upload="beforeAvatarUpload">
            <el-icon>
              <Plus/>
            </el-icon>

            <template #file="{ file }">
              <div>
                <img class="el-upload-list__item-thumbnail" :src="file.url" alt="url"/>
                <span class="el-upload-list__item-actions"></span>
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item prop="nickname" label="用户昵称">
          <el-input v-model="editForm.nickname"></el-input>
        </el-form-item>
        <el-form-item prop="password" label="用户密码">
          <el-input :prefix-icon="Lock" v-model="editForm.password" type="password"
                    autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="email" label="电子邮箱">
          <el-input v-model="editForm.email"></el-input>
        </el-form-item>
        <el-form-item prop="mobilePhone" label="手机号">
          <el-input v-model="editForm.mobilePhone"></el-input>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="editForm.sex">
            <el-radio :label="0">不公开</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm(ucvFormRef)">提交保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </el-space>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from "vue";
import type {FormInstance, UploadRawFile} from 'element-plus';
import {Lock} from "@element-plus/icons-vue";
import {getDetail, update} from "@/api/system/user-api";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import {RULE_MOBILE, RULE_EMAIL} from "@/utils/rules-util";
import {updateUserInfo} from "@/store";
import {ElMessage} from "element-plus";

const validatePhone = (rule: any, value: any, callback: any) => {
  if (!RULE_MOBILE.test(value)) {
    callback(new Error('非法手机号'));
  } else {
    callback();
  }
}

const validateEmail = (rule: any, value: any, callback: any) => {
  if (!RULE_EMAIL.test(value)) {
    callback(new Error('邮箱格式错误'));
  } else {
    callback();
  }
}

const ucvFormRef = ref<FormInstance>();
const editForm = ref<any>({});
const detailUser = ref<any>({});
// 表单校验规则
const rules = reactive({
  username: [{min: 2, max: 30, message: '长度限制2~30', trigger: 'blur'}],
  mobilePhone: [{validator: validatePhone, trigger: 'blur'}],
  email: [{validator: validateEmail, trigger: 'blur'}],
  nickname: [{min: 2, max: 30, message: '长度限制2~30', trigger: 'blur'}],
  password: [{min: 6, max: 32, message: '长度限制6~32', trigger: 'blur'}]
});
const getSex = (sex: number) => {
  switch (sex) {
    case 0: return '不公开';
    case 1: return '男';
    case 2: return '女';
  }
}

onMounted(() => {
  initUserDetail();
});

/**
 * 获取用户详细
 */
const initUserDetail = () => {
  reqCommonFeedback(getDetail(), (data: any) => {
    editForm.value = data;
    detailUser.value = Object.assign(detailUser.value, data);
    updateUserInfo(detailUser.value);
  });
}

/**
 * 表单提交
 * @param ucvFormRef
 */
const submitForm = (ucvFormRef: any) => {
  ucvFormRef.validate((valid: any) => {
    if (valid) {
      reqSuccessFeedback(update(editForm.value), '修改成功', () => {
        initUserDetail();
      });
    }
  });
}

/**
 * 头像上传成功
 */
const handleAvatarSuccess = (resp: any) => {
  editForm.value.avatar = resp.data;
}

/**
 * 头像上传前校验
 */
const beforeAvatarUpload = (rawFile: UploadRawFile) => {
  console.log(rawFile.type)
  if (rawFile.type === 'image/jpeg' || rawFile.type === 'image/png' || rawFile.type === 'image/jpg') {
    return true;
  } else {
    ElMessage.error('不支持的图片类型');
    return false;
  }
}
</script>

<style scoped>
.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}
</style>