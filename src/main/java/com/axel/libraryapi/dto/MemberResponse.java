package com.axel.libraryapi.dto;

import com.axel.libraryapi.entity.Member;
import java.time.LocalDate;

public record MemberResponse(
        Long id, String name, String email, String phone, LocalDate registeredAt, boolean active
) {
    public static MemberResponse from(Member m){
        return new MemberResponse(m.getId(), m.getName(), m.getEmail(), m.getPhone(),
                m.getRegisteredAt(), m.isActive());
    }
}
