package roomescape.service.fakedao;

import roomescape.model.Member;
import roomescape.repository.dao.MemberDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeMemberDao implements MemberDao {

    private final AtomicLong index = new AtomicLong(1);
    private final List<Member> members = new ArrayList<>();

    public FakeMemberDao(List<Member> members) {
        members.forEach(this::save);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        Member result = members.stream()
                .filter(member -> member.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> null);
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Member> findById(long id) {
        Member result = members.stream()
                .filter(member -> member.getId() == id)
                .findFirst()
                .orElseThrow(() -> null);
        return Optional.ofNullable(result);
    }

    private long save(Member rawMember) {
        long key = index.getAndIncrement();
        Member member = new Member(key, rawMember.getName(), rawMember.getEmail(), rawMember.getPassword());
        members.add(member);
        return key;
    }
}
