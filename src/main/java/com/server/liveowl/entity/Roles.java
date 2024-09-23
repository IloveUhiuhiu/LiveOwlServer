package com.server.liveowl.entity;


import jakarta.persistence.*;

import java.util.Date;
import java.util.Set;

@Entity
public class Roles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "role_anme")
    private String roleName;

    @Column(name = "create_date")
    private Date createDate;

    @OneToMany(mappedBy = "roles")
    private Set<Users> listUser;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Set<Users> getListUser() {
        return listUser;
    }

    public void setListUser(Set<Users> listUser) {
        this.listUser = listUser;
    }
}
