package roomescape.repository.dao;

import roomescape.model.Theme;

import java.util.List;
import java.util.Optional;

public interface ThemeDao {

    long save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(long id);

    void deleteById(long id);
}