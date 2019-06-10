package com.muggle.test;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @program: demo
 * @description:
 * @author: muggle
 * @create: 2019-01-03 13:59
 **/
@Data
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@Entity
@Table(name = "volidator_bean")
@DynamicUpdate
public class VolidatorBean implements Serializable {
    @Id
    @Column(name = "id")
    private Long id;
    private Integer type;
    private String field;
    private Integer slId;
}
