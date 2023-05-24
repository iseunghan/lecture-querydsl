package me.iseunghan.lecturequerydsl.dto;

import lombok.*;

@ToString
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDiffFieldDto {

    private String name;    // username이 아닌 name으로 변경
    private int myAge;  // age가 아닌 myAge로 변경
}
