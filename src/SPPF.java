import java.util.*;

public class SPPF implements Comparable<SPPF> {

    private final String value;
    private final TreeSet<List<SPPF>> children;

    private final TreeMap<SPPF, SPPF> existingSPPFs;

    public SPPF(String value, TreeMap<SPPF, SPPF> existingSPPFs) {
        this.value = value;
        this.children = new TreeSet<>(new SPPFlistComparator());
        this.existingSPPFs = existingSPPFs;
    }

    public void addChildList(List<SPPF> l) {
        for (int i = 0; i < l.size(); i++) {
            SPPF sppf = l.get(i);
            if (existingSPPFs.containsKey(sppf)) {
                l.set(i, sppf);
            } else {
                existingSPPFs.put(sppf, sppf);
            }
        }
        children.add(l);
    }

    public String walkTerminals() {
        if (children.isEmpty()) return value;
        StringBuilder res = null;
        for (List<SPPF> childlist : children) {
            StringBuilder next = new StringBuilder();
            if (res == null) res = next;
            for (SPPF child : childlist) {
                next.append(child.walkTerminals());
            }
            assert(res.toString().equals(next.toString()));
        }
        return res.toString();
    }

    @Override
    public int compareTo(SPPF o) {
        if (!this.value.equals(o.value)) return this.value.compareTo(o.value);
        if (children.size() != o.children.size()) return (children.size() < o.children.size()) ? -1 : 1;
        if (children.equals(o.children)) return 0;

        return -1;
    }
}

class SPPFlistComparator implements Comparator<List<SPPF>> {

    @Override
    public int compare(List<SPPF> o1, List<SPPF> o2) {
        if (o1.size() != o2.size()) return (o1.size() < o2.size()) ? -1 : 1;
        for (int i = 0; i < o1.size(); i++) {
            int res = o1.get(i).compareTo(o2.get(i));
            if (res != 0) return res;
        }
        return 0;
    }
}
