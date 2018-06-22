package hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import java.util.Date;

import org.hibernate.annotations.OptimisticLockType;

@Entity
//@org.hibernate.annotations.Entity(optimisticLock = OptimisticLockType.ALL)
@org.hibernate.annotations.Entity(dynamicUpdate = true)
@Table(name = "Billing", uniqueConstraints = {
        @UniqueConstraint(columnNames = "ID") })
public class Billing implements Serializable {

    private static final long serialVersionUID = -1798070786993154676L;
    /*public boolean newUser(Integer id, String password) {
            this.userID = id;
            this.password = password;
            this.time = "2015-02-30T16:22";
            return true;
    }*/

    public Integer getId() { return this.billingID;}
    public Integer getUser() { return this.userID;}
    public Integer getAmount(){ return this.amount; }
    public Date getTime(){ return this.time; }
    public Integer getState(){ return this.state; }

    public void setAmount(int summ) { this.amount = summ;}
    public void setTime(Date time) { this.time = time;}
    public void setState(Integer state) { this.state = state;}
    public void setUser(Integer id) { this.userID = id;}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    private Integer billingID;

    @Column(name="USER_ID", unique = false, nullable = false, length = 100)
    private Integer userID;

    @Column(name = "AMOUNT", unique = false, nullable = false, length = 100)
    private Integer amount;

    @Column(name = "TIME", unique = false, nullable = false, length = 100)
    private Date time;

    @Column(name = "STATE", unique = false, nullable = false, length = 100)
    private Integer state;

}
