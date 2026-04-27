package in.co.sagarfood.sagarexpense;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLHelper {
    public static class root{
        public UserInfo ui;
    }
    public static class UserInfo {
        private boolean isValid = false;

        public UserInfo() {}
        public UserInfo(boolean isValid) { this.isValid = isValid; }

        private int sno, godownNo;
        private double ttlExpense;
        private String userid, username, password, mobile, usertype, status, godownName;

        public boolean isValid() {
            return isValid;
        }

        public void setValid(boolean valid) {
            isValid = valid;
        }

        public int getSno() {
            return sno;
        }

        public void setSno(int sno) {
            this.sno = sno;
        }

        public int getGodownNo() {
            return godownNo;
        }

        public void setGodownNo(int godownNo) {
            this.godownNo = godownNo;
        }

        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getUsertype() {
            return usertype;
        }

        public void setUsertype(String usertype) {
            this.usertype = usertype;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getGodownName() {
            return godownName;
        }

        public void setGodownName(String godownName) {
            this.godownName = godownName;
        }

        public double getTtlExpense() {
            return ttlExpense;
        }

        public void setTtlExpense(double ttlExpense) {
            this.ttlExpense = ttlExpense;
        }
    }
}
