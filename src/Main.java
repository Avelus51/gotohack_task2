import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private enum StructureIndices {
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

    private enum EventIndices {
        USER_ID,
        ACTION,
        STEP_ID,
        TIME
    }

    static class AbilityToReturn implements Comparable<AbilityToReturn>{
        int stepId;
        int returns;
        int uniqueVisitors;
        double returnRate;

        AbilityToReturn(int stepId) {
            this.stepId = stepId;
        }

        @Override
        public int compareTo(AbilityToReturn abilityToReturn) {
            double returnRate = (double) returns/uniqueVisitors;
            double thatReturnRate = (double) abilityToReturn.returns/abilityToReturn.uniqueVisitors;
            return Double.compare(returnRate, thatReturnRate);
        }

        @Override
        public String toString() {
            return Integer.valueOf(stepId).toString();
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            AbilityToReturn that = (AbilityToReturn) o;
            return stepId == that.stepId;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(stepId);
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
        Path eventsPath = Paths.get("res/course-217-events.csv");
        scanner = new Scanner(eventsPath);
        scanner.nextLine();
        List<String[]> events = new ArrayList<>();
        while (scanner.hasNextLine()) {
            events.add(scanner.nextLine().split(","));
        }
        Collections.reverse(events);
        ///////////////////////
        List<Set<Integer>> visitors = new ArrayList<>();
        List<Set<Integer>> returnedVisitors = new ArrayList<>();
        steps.forEach(s -> {
            visitors.add(new HashSet<>());
            returnedVisitors.add(new HashSet<>());
        });
        List<AbilityToReturn> abilityToReturnList = new ArrayList<>();
        for (int i = 0; i < steps.size()-1 ; i++)
        {
            int stepId = Integer.parseInt(steps.get(i)[StructureIndices.STEP_ID.ordinal()]);
            int stepIndex = stepsIndices.get(stepId);
            abilityToReturnList.add(stepIndex, new AbilityToReturn(stepId));
        }
        events.forEach( e -> {
            int userId = Integer.parseInt(e[EventIndices.USER_ID.ordinal()]);
            int stepId = Integer.parseInt(e[EventIndices.STEP_ID.ordinal()]);
            int stepIndex = stepsIndices.get(stepId);
            //Returns to last step don't exist
            if (stepIndex < steps.size()-1) {
                if (visitors.get(stepIndex).contains(userId)
                        && visitors.get(stepIndex + 1).contains(userId)
                        && !returnedVisitors.get(stepIndex).contains(userId)) {
                    //FIXME
                    abilityToReturnList.get(stepIndex).returns++;
                    returnedVisitors.get(stepIndex).add(userId);
                }
                else {
                    visitors.get(stepIndex).add(userId);
                }
            }
        });
        abilityToReturnList.forEach( a -> {
            int stepIndex = stepsIndices.get(a.stepId);
            a.uniqueVisitors = visitors.get(stepIndex).size();
        });
        ///////////////////////
        Collections.sort(abilityToReturnList, Comparator.reverseOrder());
        System.out.println(abilityToReturnList.stream()
                .sorted(Comparator.reverseOrder())
                .limit(10)
                .map(Object::toString)
                .collect(Collectors.joining(",")));
    }
}
