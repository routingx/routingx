package routingx.manager;

import routingx.service.UniversalAbsService;
import routingx.service.UniversalService;

public final class UniversalManargeImpl extends UniversalAccessImpl implements UniversalManager {

	private UniversalService access;

	public UniversalManargeImpl(UniversalService access) {
		this.access = access;
	}

	@Override
	protected UniversalAbsService access() {
		return access;
	}

}
