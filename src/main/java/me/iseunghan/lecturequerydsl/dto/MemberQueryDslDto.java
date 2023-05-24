package me.iseunghan.lecturequerydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

@ToString
@Getter @Setter
@NoArgsConstructor
public class MemberQueryDslDto {

    private String username;
    private int age;

    @QueryProjection
    public MemberQueryDslDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
