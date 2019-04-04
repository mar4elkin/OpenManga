package org.nv95.openmanga.adapters;

import org.nv95.openmanga.items.MangaChapter;

public interface OnChapterClickListener {
    void onChapterClick(int pos, MangaChapter chapter);
    boolean onChapterLongClick(int pos, MangaChapter chapter);
}
