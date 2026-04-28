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
        private double ttlExpense, ttlIncome;
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

        public double getTtlIncome() {
            return ttlIncome;
        }

        public void setTtlIncome(double ttlIncome) {
            this.ttlIncome = ttlIncome;
        }
    }

    public static class ExpenseInfo{
        int sno, godownNo, userno;
        String DateOfExpense, ExpenseName, ExpenseBY, submittedOn,submittedBY,supportingBill,signsrc,verifiedByAdmin,status;
        double ExpenseAmount;
        boolean isSelected;
        public boolean isSelected() {
            return isSelected;
        }
        public void setSelected(boolean selected) {
            isSelected = selected;
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

        public int getUserno() {
            return userno;
        }

        public void setUserno(int userno) {
            this.userno = userno;
        }

        public String getDateOfExpense() {
            return DateOfExpense;
        }

        public void setDateOfExpense(String dateOfExpense) {
            DateOfExpense = dateOfExpense;
        }

        public String getExpenseName() {
            return ExpenseName;
        }

        public void setExpenseName(String expenseName) {
            ExpenseName = expenseName;
        }

        public String getExpenseBY() {
            return ExpenseBY;
        }

        public void setExpenseBY(String expenseBY) {
            ExpenseBY = expenseBY;
        }

        public String getSubmittedOn() {
            return submittedOn;
        }

        public void setSubmittedOn(String submittedOn) {
            this.submittedOn = submittedOn;
        }

        public String getSubmittedBY() {
            return submittedBY;
        }

        public void setSubmittedBY(String submittedBY) {
            this.submittedBY = submittedBY;
        }

        public String getSupportingBill() {
            return supportingBill;
        }

        public void setSupportingBill(String supportingBill) {
            this.supportingBill = supportingBill;
        }

        public String getSignsrc() {
            return signsrc;
        }

        public void setSignsrc(String signsrc) {
            this.signsrc = signsrc;
        }

        public String getVerifiedByAdmin() {
            return verifiedByAdmin;
        }

        public void setVerifiedByAdmin(String verifiedByAdmin) {
            this.verifiedByAdmin = verifiedByAdmin;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public double getExpenseAmount() {
            return ExpenseAmount;
        }

        public void setExpenseAmount(double expenseAmount) {
            ExpenseAmount = expenseAmount;
        }

    }
    public static class GodownInfo {
        int sno;
        String name;

        public int getSno() {
            return sno;
        }

        public void setSno(int sno) {
            this.sno = sno;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name; // Spinner shows name
        }
    }
}
