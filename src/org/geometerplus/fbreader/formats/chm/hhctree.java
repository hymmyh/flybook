package org.geometerplus.fbreader.formats.chm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hym E-mail:hymmyh@gmail.com
 * @version 创建时间：2011-2-27 上午10:12:04
 * 类说明 用来储藏 hhc 中每个节点
 */
public class hhctree {
public String name="";
public String filename="";
public int num=0;
public List<hhctree> child=new ArrayList();
public hhctree parent=null;

}
