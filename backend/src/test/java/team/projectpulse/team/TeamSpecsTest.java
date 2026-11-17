package team.projectpulse.team;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * The team search lowercases the search term in Java and compares it with the database's LOWER(...) of the column.
 * The Java side must not depend on the server's default locale: under Turkish, "I".toLowerCase() is a dotless
 * "ı", which LOWER(...) in MySQL never produces, so a search for "INFO" would match no team.
 */
@ExtendWith(MockitoExtension.class)
class TeamSpecsTest {

    @Mock
    Root<Team> root;

    @Mock
    CriteriaQuery<?> query;

    @Mock
    CriteriaBuilder criteriaBuilder;

    @Mock
    Path<Object> teamNamePath;

    @Mock
    Expression<String> loweredTeamName;

    private Locale originalLocale;

    @BeforeEach
    void setUp() {
        this.originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
    }

    @AfterEach
    void tearDown() {
        Locale.setDefault(this.originalLocale);
    }

    @Test
    @SuppressWarnings("unchecked")
    void containsTeamNameLowercasesIndependentlyOfTheDefaultLocale() {
        // Given
        given(this.root.get("teamName")).willReturn(this.teamNamePath);
        given(this.criteriaBuilder.lower(any(Expression.class))).willReturn(this.loweredTeamName);

        // When
        TeamSpecs.containsTeamName("INFO").toPredicate(this.root, this.query, this.criteriaBuilder);

        // Then
        verify(this.criteriaBuilder).like(this.loweredTeamName, "%info%");
    }

    @Test
    @SuppressWarnings("unchecked")
    void hasSectionNameLowercasesIndependentlyOfTheDefaultLocale() {
        // Given
        Path<Object> sectionPath = org.mockito.Mockito.mock(Path.class);
        Path<Object> sectionNamePath = org.mockito.Mockito.mock(Path.class);
        given(this.root.get("section")).willReturn(sectionPath);
        given(sectionPath.get("sectionName")).willReturn(sectionNamePath);
        given(this.criteriaBuilder.lower(any(Expression.class))).willReturn(this.loweredTeamName);

        // When
        TeamSpecs.hasSectionName("FINAL ITERATION").toPredicate(this.root, this.query, this.criteriaBuilder);

        // Then
        verify(this.criteriaBuilder).equal(this.loweredTeamName, "final iteration");
    }

}
