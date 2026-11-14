package team.projectpulse.student;

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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 * The student search lowercases both sides of the comparison: the column with the database's LOWER(), the search
 * term with Java. The Java side must not depend on the server's default locale. Under a Turkish locale,
 * "I".toLowerCase() is the dotless "ı", so a search for "LISA" or "J.SMITH@ABC.EDU" would never match a row.
 */
@ExtendWith(MockitoExtension.class)
class StudentSpecsTest {

    @Mock
    Root<Student> root;

    @Mock
    CriteriaQuery<?> query;

    @Mock
    CriteriaBuilder criteriaBuilder;

    @Mock
    Path<String> column;

    @Mock
    Expression<String> loweredColumn;

    private Locale originalDefault;

    @BeforeEach
    void setUp() {
        this.originalDefault = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
    }

    @AfterEach
    void tearDown() {
        Locale.setDefault(this.originalDefault);
    }

    @Test
    void testContainsFirstNameLowercasesIndependentOfDefaultLocale() {
        doReturn(this.column).when(this.root).get("firstName");
        given(this.criteriaBuilder.lower(this.column)).willReturn(this.loweredColumn);

        StudentSpecs.containsFirstName("LISA").toPredicate(this.root, this.query, this.criteriaBuilder);

        verify(this.criteriaBuilder).like(this.loweredColumn, "%lisa%");
    }

    @Test
    void testHasEmailLowercasesIndependentOfDefaultLocale() {
        doReturn(this.column).when(this.root).get("email");
        given(this.criteriaBuilder.lower(this.column)).willReturn(this.loweredColumn);

        StudentSpecs.hasEmail("J.SMITH@ABC.EDU").toPredicate(this.root, this.query, this.criteriaBuilder);

        verify(this.criteriaBuilder).equal(this.loweredColumn, "j.smith@abc.edu");
    }

}
