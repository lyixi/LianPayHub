package com.lianpayhub.domain.website;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "website_lead")
public class WebsiteLead extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=80) private String name;
    @Column(nullable=false,length=128) private String contact;
    @Column(length=160) private String subject;
    @Column(nullable=false,length=2000) private String message;
    @Column(nullable=false,length=24) private String status="NEW";
    protected WebsiteLead() {}
    public WebsiteLead(String name,String contact,String subject,String message){this.name=name;this.contact=contact;this.subject=subject;this.message=message;}
    public Long getId(){return id;} public String getName(){return name;} public String getContact(){return contact;} public String getSubject(){return subject;} public String getMessage(){return message;} public String getStatus(){return status;} public void changeStatus(String status){this.status=status;}
}
