package hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.Date;

import org.hibernate.annotations.OptimisticLockType;

@Entity
//@org.hibernate.annotations.Entity(optimisticLock = OptimisticLockType.ALL)
@org.hibernate.annotations.Entity(dynamicUpdate = true)
@Table(name = "Users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "ID") })
public class User implements Serializable {

    private static final long serialVersionUID = -1798070786993154676L;
    public boolean newUser(String mail, String password) {
            this.email = mail;
            this.password = password;
            //this.time = new"2015-02-30T16:22";
            this.favouritesList =  new ArrayList<Integer>();
            return true;
    }

    public void setMail(String mail) {
            this.email = mail;
    }

    public Date getTime(){ return this.time; }
    public ArrayList<Integer> getFL(){ return this.favouritesList; }
    public Integer getId() { return this.userId;}
    public String getName() { return this.email;}
    public void setSocial(String social_type) { this.social_type = social_type;}
    public void setSocial_ID(Integer social_id) { this.social_id = social_id;}

    public String getSocial() { return this.social_type;}
    public Integer getSocial_ID() { return this.social_id;}

    public void setPass(String password) { this.password = password;}
    public void setTime(Date time) { this.time = time; }
    public void setFL(ArrayList<Integer> favouritesList) {this.favouritesList = favouritesList;}

    public void addDays(int days){
        if (new Date().compareTo(this.time) > 0){
                this.time = new Date(new Date().getTime() + days * 86400000);
        } else if(new Date().compareTo(this.time) < 0){
            this.time = new Date(this.time.getTime() + days * 86400000);
        }
    }

    public void addFav(int id){
        if(!this.favouritesList.contains(id)){
            this.favouritesList.add(0, id);
        }
    }

    public void removeFav(int id){
        if(this.favouritesList.contains(id)){
            this.favouritesList.remove(this.favouritesList.indexOf(id));
        }
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    private Integer userId;

    @Column(name = "EMAIL", unique = false, nullable = true, length = 100)
    private String email;

    @Column(name = "PASSWORD", unique = false, nullable = true, length = 100)
    private String password;

    @Column(name = "TIME", unique = false, nullable = false, length = 100)
    private Date time;

    @Column(name = "FAVOURITES_LIST", unique = false, nullable = false, length = 100)
    private ArrayList<Integer> favouritesList;

    @Column(name = "SOCIAL_NET", unique = false, nullable = true, length = 100)
    private String social_type;

    @Column(name = "SOCIAL_ID", unique = false, nullable = true, length = 100)
    private Integer social_id;

    // Accessors and mutators for all four fields
}
