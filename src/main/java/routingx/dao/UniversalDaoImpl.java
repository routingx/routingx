package routingx.dao;

import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class UniversalDaoImpl extends UniversalNativeDaoImpl implements UniversalDao {

	public UniversalDaoImpl(SimpleRepositoryFactory repositoryFactory) {
		this.setFactory(repositoryFactory);
	}

}
