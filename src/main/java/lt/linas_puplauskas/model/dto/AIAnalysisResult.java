package lt.linas_puplauskas.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lt.linas_puplauskas.model.TicketCategory;
import lt.linas_puplauskas.model.TicketPriority;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResult {
    private boolean shouldBeTicket;
    private String title;
    private TicketCategory category;
    private TicketPriority priority;
    private String summary;
}
