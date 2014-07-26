package net.pms.dlna;

import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalIdRepo {
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalIdRepo.class);

	private int globalId = 0, deletions = 0;

	class ID {
		int id;
		DLNAResource dlna;
		ID(DLNAResource d) {
			id = globalId++;
			d.setIndexId(id);
			dlna = d;
		}
	}
	private ArrayList<ID> ids = new ArrayList<>();

	public GlobalIdRepo() {
	}

	public synchronized void add(DLNAResource d) {
		String id = d.getId();
		if (id != null) {
			remove(id);
		}
		ids.add(new ID(d));
	}

	public DLNAResource get(String id) {
		return get(parseIndex(id));
	}

	public DLNAResource get(int id) {
		int index = indexOf(id);
		return index > -1 ? ids.get(index).dlna : null;
	}

	public void remove(DLNAResource d) {
		remove(d.getId());
	}

	public void remove(String id) {
		remove(parseIndex(id));
	}

	public synchronized void remove(int id) {
		int index = indexOf(id);
		if (index > -1) {
			LOGGER.debug("GlobalIdRepo: removing id {} - {}", id, ids.get(index).dlna.getName());
			ids.remove(index);
			deletions++;
		}
	}

	public int parseIndex(String id) {
		try {
			// Id strings may have optional tags beginning with $ appended, e.g. '1234$Temp'
			return Integer.parseInt(StringUtils.substringBefore(id, "$"));
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private int indexOf(int id) {
		if (id < globalId) {
			// We're in sequence by definition, so binary search is quickest

			// Exclude any areas where the id can't possibly be
			int ceil = ids.size() - 1;
			int hi = id < ceil ? id : ceil;
			int floor = hi - deletions;
			int lo = floor > 0 ? floor : 0;

			while (lo <= hi) {
				int mid = lo + (hi - lo) / 2;
				int idm = ids.get(mid).id;
				if (id < idm) {
					hi = mid - 1;
				} else if (id > idm) {
					lo = mid + 1;
				} else {
					return mid;
				}
			}
		}
		return -1;
	}
}