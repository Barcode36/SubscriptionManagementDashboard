package it.arsinfo.smd.ui.incassa.abbonamento;

import java.util.EnumSet;
import java.util.List;

import com.vaadin.data.Binder;
import com.vaadin.data.converter.LocalDateToDateConverter;
import com.vaadin.data.converter.StringToBigDecimalConverter;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

import it.arsinfo.smd.data.Anno;
import it.arsinfo.smd.data.Cassa;
import it.arsinfo.smd.data.Ccp;
import it.arsinfo.smd.data.Cuas;
import it.arsinfo.smd.data.Incassato;
import it.arsinfo.smd.data.StatoAbbonamento;
import it.arsinfo.smd.entity.Abbonamento;
import it.arsinfo.smd.entity.Anagrafica;
import it.arsinfo.smd.entity.Campagna;
import it.arsinfo.smd.service.Smd;
import it.arsinfo.smd.ui.vaadin.SmdItemEditor;

public class IncassaAbbonamentoEditor extends SmdItemEditor<Abbonamento> {

    private boolean noOmaggio;
    private boolean hasResiduo;

    private final ComboBox<Anagrafica> intestatario = new ComboBox<Anagrafica>("Intestatario");
    private final ComboBox<Campagna> campagna = new ComboBox<Campagna>("Campagna");
    private final ComboBox<StatoAbbonamento> statoAbbonamento = new ComboBox<StatoAbbonamento>("Stato",
            EnumSet.allOf(StatoAbbonamento.class));

    private final ComboBox<Anno> anno = new ComboBox<Anno>("Selezionare Anno",
            EnumSet.allOf(Anno.class));

    private final TextField importo = new TextField("Importo");
    private final TextField spese = new TextField("Spese");
    private final TextField speseEstero = new TextField("Spese Estero");
    private final TextField speseEstrattoConto = new TextField("Spese Estratto Conto");
    private final TextField pregresso = new TextField("Pregresso");
    private final TextField totale = new TextField("Totale");
    private final TextField residuo = new TextField("Residuo");
    private final TextField incassato = new TextField("Incassato");
    private final ComboBox<Cassa> cassa = new ComboBox<Cassa>("Cassa",
            EnumSet.allOf(Cassa.class));
    private final TextField codeLine = new TextField("Code Line");
    private final ComboBox<Ccp> ccp = new ComboBox<Ccp>("Conto Corrente",
            EnumSet.allOf(Ccp.class));
    private final ComboBox<Cuas> cuas = new ComboBox<Cuas>("Cuas",
            EnumSet.allOf(Cuas.class));
    private final TextField progressivo = new TextField("Progressivo");

    private final ComboBox<Incassato> statoIncasso = new ComboBox<Incassato>("Incassato",EnumSet.allOf(Incassato.class));
    
    private final DateField dataContabile = new DateField("Data contabile");
    private final DateField dataPagamento = new DateField("Data pagamento");
    
    public IncassaAbbonamentoEditor(List<Anagrafica> anagrafica, List<Campagna> campagne) {

        super(new Binder<>(Abbonamento.class));
        
        HorizontalLayout anag = new HorizontalLayout(campagna,anno,codeLine);
        anag.addComponentsAndExpand(intestatario);

        HorizontalLayout status = new HorizontalLayout(statoAbbonamento,statoIncasso);
        
        HorizontalLayout imp = new HorizontalLayout(importo,speseEstero,spese,pregresso,speseEstrattoConto);
        HorizontalLayout res =	new HorizontalLayout(totale,incassato,residuo);

        HorizontalLayout incss = new HorizontalLayout(dataContabile,dataPagamento,cassa,ccp,cuas);
        HorizontalLayout detai = new HorizontalLayout();
        detai.addComponentsAndExpand(progressivo);
        setComponents(anag, status,imp,res,incss,detai);


        intestatario.setItems(anagrafica);
        intestatario.setItemCaptionGenerator(Anagrafica::getCaption);
        intestatario.setEmptySelectionAllowed(false);


        campagna.setItems(campagne);
        campagna.setItemCaptionGenerator(Campagna::getCaption);
 
        anno.setItemCaptionGenerator(Anno::getAnnoAsString);
        anno.setEmptySelectionAllowed(false);
        
        campagna.setReadOnly(true);
        anno.setReadOnly(true);
        codeLine.setReadOnly(true);
        intestatario.setReadOnly(true);

        statoAbbonamento.setReadOnly(true);
        statoIncasso.setReadOnly(true);
        
        importo.setReadOnly(true);
        speseEstero.setReadOnly(true);
        spese.setReadOnly(true);
        pregresso.setReadOnly(true);
        speseEstrattoConto.setReadOnly(true);

        totale.setReadOnly(true);        
        incassato.setReadOnly(true);
        residuo.setReadOnly(true);

        cassa.setEmptySelectionAllowed(false);
        ccp.setEmptySelectionAllowed(false);
        ccp.setItemCaptionGenerator(Ccp::getCcp);
        cuas.setEmptySelectionAllowed(false);
        cuas.setItemCaptionGenerator(Cuas::getDenominazione);

        dataContabile.setDateFormat("dd/MM/yyyy");
        dataPagamento.setDateFormat("dd/MM/yyyy");

        getBinder().forField(codeLine).asRequired().withValidator(ca -> ca != null,
                "Deve essere definito").bind(Abbonamento::getCodeLine,
                                             Abbonamento::setCodeLine);
        getBinder().forField(intestatario)
            .asRequired()
            .withValidator(an -> an != null,"Scegliere un Cliente")
            .bind(Abbonamento::getIntestatario,Abbonamento::setIntestatario);

        getBinder().forField(campagna).bind(Abbonamento::getCampagna, Abbonamento::setCampagna);
        getBinder().forField(anno).asRequired().bind("anno");
        getBinder().forField(statoAbbonamento).bind("statoAbbonamento");
        getBinder().forField(statoIncasso).bind("statoIncasso");


        getBinder()
        .forField(importo)
        .withConverter(new StringToBigDecimalConverter("Conversione in Eur"))
        .bind("importo");
        getBinder()
        .forField(spese)
        .withConverter(new StringToBigDecimalConverter("Conversione in Eur"))
        .withValidator(sp -> sp != null, "Spese non può essere null")
        .bind("spese");
        getBinder()
        .forField(speseEstero)
        .withConverter(new StringToBigDecimalConverter("Conversione in Eur"))
        .withValidator(sp -> sp != null, "Spese Estero non può essere null")
        .bind("speseEstero");
        getBinder()
        .forField(speseEstrattoConto)
        .withConverter(new StringToBigDecimalConverter("Conversione in Eur"))
        .withValidator(sp -> sp != null, "Spese Estratto conto non può essere null")
        .bind("speseEstrattoConto");
        getBinder()
        .forField(pregresso)
        .withConverter(new StringToBigDecimalConverter("Conversione in Eur"))
        .withValidator(sp -> sp != null, "Pregresso non può essere null")
        .bind("pregresso");
        getBinder()
        .forField(totale)
        .withConverter(new StringToBigDecimalConverter("Conversione in Eur"))
        .bind("totale");

        getBinder()
        .forField(incassato)
        .withConverter(new StringToBigDecimalConverter("Conversione in Eur"))
        .bind("incassato");
        getBinder()
            .forField(residuo)
            .withConverter(new StringToBigDecimalConverter("Conversione in Eur"))
            .bind("residuo");
               
        getBinder().forField(dataContabile).asRequired()
        .withConverter(new LocalDateToDateConverter())
        .bind("dataContabile");
        
        getBinder().forField(dataPagamento).asRequired()
        .withConverter(new LocalDateToDateConverter())
        .bind("dataPagamento");
        getBinder().forField(cassa).bind("cassa");
        getBinder().forField(ccp).bind("ccp");
        getBinder().forField(cuas).bind("cuas");                
        getBinder().forField(progressivo).asRequired().bind("progressivo");  
        
    }

    @Override
    public void focus(boolean persisted, Abbonamento abbonamento) {
        
        codeLine.setVisible(persisted);
        campagna.setVisible(persisted);

        noOmaggio = Smd.getStatoIncasso(abbonamento) != Incassato.Omaggio;
        hasResiduo = abbonamento.getResiduo().signum() > 0; 

        importo.setVisible(noOmaggio);
        spese.setVisible(noOmaggio);
        speseEstero.setVisible(noOmaggio);
        speseEstrattoConto.setVisible(noOmaggio);
        pregresso.setVisible(noOmaggio);
        totale.setVisible(noOmaggio);
        incassato.setVisible(noOmaggio);
        residuo.setVisible(noOmaggio);
        
        dataContabile.setVisible(noOmaggio && hasResiduo);
        dataPagamento.setVisible(noOmaggio && hasResiduo); 
        cassa.setVisible(noOmaggio && hasResiduo);
        ccp.setVisible(noOmaggio && hasResiduo);
        cuas.setVisible(noOmaggio && hasResiduo);
        progressivo.setVisible(noOmaggio && hasResiduo);
                
        progressivo.focus();

    }
        
    public boolean incassare() {
        return noOmaggio && hasResiduo;
    }
}
