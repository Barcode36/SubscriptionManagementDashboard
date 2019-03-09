package it.arsinfo.smd.vaadin.ui.subsearch;

import java.util.List;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;

import it.arsinfo.smd.entity.SmdEntity;
import it.arsinfo.smd.vaadin.SmdChangeHandler;

public abstract class SmdSubSearch<T extends SmdEntity, K extends SmdEntity>
        extends SmdChangeHandler {

    /**
     * 
     */
    private static final long serialVersionUID = 7884064928998716106L;

    private final Grid<T> grid;
    private K upper;
    private T selected;
    private Button add = new Button("New", VaadinIcons.PLUS);

    public SmdSubSearch(Grid<T> grid) {
        this.grid = grid;
        this.grid.setWidth("80%");

        this.grid.asSingleSelect().addValueChangeListener(e -> {
            selected = e.getValue();
            onChange();
        });

        add.addClickListener(e -> {
            selected = generate();
            onChange();
        });

    }

    public void onSearch() {
        grid.setItems(search());
    }

    public abstract T generate();

    public abstract List<T> search();

    public Grid<T> getGrid() {
        return grid;
    }
    
    public void setColumns(String...columnIds) {
        grid.setColumns(columnIds);
    }

    public void setColumnCamptio(String columnId, String caption) {
        if (grid.getColumn(columnId) == null) {
            return;
        }
        grid.getColumn(columnId).setCaption(caption);
    }

    
    public T getSelected() {
        return selected;
    }

    public Button getAdd() {
        return add;
    }

    public K getUpper() {
        return upper;
    }

    public void setUpper(K upper) {
        this.upper = upper;
    }
    
}
