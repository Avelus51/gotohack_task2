import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    enum StructureIndices {
        COURSE_ID,
        MODULE_ID,
        MODULE_POSITION,
        LESSON_ID,
        LESSON_POSITION,
        STEP_ID,
        STEP_POSITION,
        STEP_TYPE,
        STEP_COST
    }

    enum EventIndices {
        USER_ID,
        ACTION,
        STEP_ID,
        TIME
    }

    static class VisitTime {
        private long first;
        private long last;

        public VisitTime(long first, long last) {
            this.first = first;
            this.last = last;
        }
    }

    static class AbilityToReturn implements Comparable<AbilityToReturn>{
        int stepId;
        double returnRate;
        double returns;
        double uniqueVisitors;

        public AbilityToReturn(int stepId) {
            this.stepId = stepId;
        }

        @Override
        public int compareTo(AbilityToReturn abilityToReturn) {
            return Double.compare(this.returnRate, abilityToReturn.returnRate);
        }

        @Override
        public String toString() {
            return Integer.valueOf(stepId).toString();
        }
    }

    public static void main(String[] args) throws Exception {
        Path structurePath = Paths.get("res/course-217-structure.csv");
        Scanner scanner = new Scanner(structurePath);
        scanner.nextLine();
        List<String[]> steps = new ArrayList<>();
        while (scanner.hasNextLine()) {
            steps.add(scanner.nextLine().split(","));
        }

        steps.sort((s1, s2) -> {
            int modulePosition1 = Integer.parseInt(s1[StructureIndices.MODULE_POSITION.ordinal()]);
            int modulePosition2 = Integer.parseInt(s2[StructureIndices.MODULE_POSITION.ordinal()]);
            int lessonPosition1 = Integer.parseInt(s1[StructureIndices.LESSON_POSITION.ordinal()]);
            int lessonPosition2 = Integer.parseInt(s2[StructureIndices.LESSON_POSITION.ordinal()]);
            int stepPosition1 = Integer.parseInt(s1[StructureIndices.STEP_POSITION.ordinal()]);
            int stepPosition2 = Integer.parseInt(s2[StructureIndices.STEP_POSITION.ordinal()]);

            if (Integer.compare(modulePosition1, modulePosition2) == 0) {
                if (Integer.compare(lessonPosition1, lessonPosition2) == 0) {
                    return Integer.compare(stepPosition1, stepPosition2);
                } else return Integer.compare(lessonPosition1, lessonPosition2);
            } else return Integer.compare(modulePosition1, modulePosition2);
        });
        Map<Integer, Integer> stepsIndices = new HashMap<>();
        for (int i = 0; i < steps.size(); i++) {
            int stepId = Integer.parseInt(steps.get(i)[StructureIndices.STEP_ID.ordinal()]);
            stepsIndices.put(stepId, i);
        }
        List<Map<Integer, VisitTime>> visits = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            visits.add(new HashMap<>());
        }
        Path eventsPath = Paths.get("res/course-217-events.csv");
        scanner = new Scanner(eventsPath);
        scanner.nextLine();
        List<String[]> events = new ArrayList<>();
        while (scanner.hasNextLine()) {
            events.add(scanner.nextLine().split(","));
        }
        Collections.reverse(events);
        List<AbilityToReturn> abilityToReturnList = new ArrayList<>();
        List<Set<Integer>> returnedVisitors = new ArrayList<>();
        for (int i = 0; i < steps.size()-1 ; i++) {
            int stepIndex = Integer.parseInt(steps.get(i)[StructureIndices.STEP_ID.ordinal()]);
            abilityToReturnList.add(new AbilityToReturn(stepIndex));
            returnedVisitors.add(new HashSet<>());
        }
        events.forEach(e -> {
            int userId = Integer.parseInt(e[EventIndices.USER_ID.ordinal()]);
            int stepId = Integer.parseInt(e[EventIndices.STEP_ID.ordinal()]);
            int stepIndex = stepsIndices.get(stepId);
            long time = Long.parseLong(e[EventIndices.TIME.ordinal()]);

            Map<Integer, VisitTime> stepVisits = visits.get(stepIndex);
            if (stepVisits.containsKey(userId)) {
                stepVisits.get(userId).last=time;
            } else {
                stepVisits.put(userId, new VisitTime(time, time));
            }
            if (stepIndex < steps.size() - 1 && visits.get(stepIndex+1).containsKey(userId)) {
                long timeOfNextStepLastVisiting = visits.get(stepIndex+1).get(userId).last;
                if (!returnedVisitors.get(stepIndex).contains(userId)
                        &&timeOfNextStepLastVisiting > stepVisits.get(userId).first
                        &&timeOfNextStepLastVisiting < stepVisits.get(userId).last) {
                    abilityToReturnList.get(stepIndex).returns++;
                    returnedVisitors.get(stepIndex).add(userId);
                }
            }
        });
        for (int i = 0; i < steps.size() - 1 ; i++) {
            AbilityToReturn abilityToReturn = abilityToReturnList.get(i);
            abilityToReturn.uniqueVisitors = visits.get(i).size();
            abilityToReturn.returnRate = abilityToReturn.returns/abilityToReturn.uniqueVisitors;
        }

        System.out.println(abilityToReturnList.stream()
                .sorted(Comparator.reverseOrder())
                .limit(10)
                .map(Object::toString)
                .collect(Collectors.joining(",")));
    }
}
