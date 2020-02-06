package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.repository.NoRepositoryBean;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SubMultipleReference;

//@NoRepositoryBean
public interface SubMultipleRefRepository<T extends SubMultipleReference> {
    T findTopByMultipleRefOrderByRefDesc(String multipleRef);
}