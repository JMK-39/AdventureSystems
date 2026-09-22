package dev.xyat.adventuresystems.curios.paradiselost.data;

import dev.xyat.adventuresystems.curios.config.CuriosConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class ParadiseLostCurve {
    public static final List<String> DEFAULT_DEFINITIONS = List.of(
            "0|0.0",
            "1000|0.60",
            "3000|1.20",
            "7000|1.60",
            "15000|1.90",
            "30000|2.00"
    );

    private static volatile List<String> cachedDefinitions = List.of();
    private static volatile List<Point> cachedPoints = parseStrict(DEFAULT_DEFINITIONS);

    private ParadiseLostCurve() {
    }

    public static double bonus(int score) {
        List<Point> points = points();
        Point first = points.get(0);
        if (score <= first.score()) return first.bonus();

        Point last = points.get(points.size() - 1);
        if (score >= last.score()) return last.bonus();

        for (int i = 0; i < points.size() - 1; i++) {
            Point from = points.get(i);
            Point to = points.get(i + 1);
            if (score >= from.score() && score < to.score()) {
                double progress = (double) (score - from.score()) / (to.score() - from.score());
                double curved = applyCurve(progress);
                return from.bonus() + (to.bonus() - from.bonus()) * curved;
            }
        }
        return last.bonus();
    }

    public static int nextTarget(int score) {
        List<Point> points = points();
        for (Point point : points) {
            if (point.score() > score) return point.score();
        }
        return points.get(points.size() - 1).score();
    }

    public static int maxScore() {
        List<Point> points = points();
        return points.get(points.size() - 1).score();
    }

    public static boolean isMaxed(int score) {
        return score >= maxScore();
    }

    public static boolean isValidDefinitionList(List<String> definitions) {
        try {
            parseStrict(definitions);
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static List<Point> points() {
        List<String> source = CuriosConfig.plCurvePoints;
        List<String> snapshot = source == null ? List.of() : List.copyOf(source);
        if (snapshot.equals(cachedDefinitions)) return cachedPoints;
        synchronized (ParadiseLostCurve.class) {
            if (snapshot.equals(cachedDefinitions)) return cachedPoints;
            List<Point> parsed;
            try {
                parsed = parseStrict(snapshot);
            } catch (RuntimeException ignored) {
                parsed = parseStrict(DEFAULT_DEFINITIONS);
                snapshot = DEFAULT_DEFINITIONS;
            }
            cachedDefinitions = List.copyOf(snapshot);
            cachedPoints = parsed;
            return cachedPoints;
        }
    }

    private static double applyCurve(double progress) {
        double p = Math.max(0.0D, Math.min(1.0D, progress));
        String mode = CuriosConfig.plCurveMode == null
                ? "sine_ease_out"
                : CuriosConfig.plCurveMode.trim().toLowerCase(Locale.ROOT);
        return switch (mode) {
            case "linear" -> p;
            case "smoothstep" -> p * p * (3.0D - 2.0D * p);
            case "power" -> Math.pow(p, Math.max(0.000001D, CuriosConfig.plCurvePower));
            default -> Math.sin(p * Math.PI / 2.0D);
        };
    }

    private static List<Point> parseStrict(List<String> definitions) {
        if (definitions == null || definitions.size() < 2) {
            throw new IllegalArgumentException("Paradise Lost curve requires at least two points");
        }
        List<Point> points = new ArrayList<>(definitions.size());
        for (String definition : definitions) {
            if (definition == null) throw new IllegalArgumentException("Null curve point");
            String[] parts = definition.trim().split("\\|", -1);
            if (parts.length != 2) throw new IllegalArgumentException("Invalid curve point: " + definition);
            int score = Integer.parseInt(parts[0].trim());
            double bonus = Double.parseDouble(parts[1].trim());
            if (score < 0 || !Double.isFinite(bonus)) {
                throw new IllegalArgumentException("Invalid curve point: " + definition);
            }
            points.add(new Point(score, bonus));
        }
        points.sort(Comparator.comparingInt(Point::score));
        if (points.get(0).score() != 0) {
            throw new IllegalArgumentException("Paradise Lost curve must start at score 0");
        }
        for (int i = 1; i < points.size(); i++) {
            if (points.get(i - 1).score() == points.get(i).score()) {
                throw new IllegalArgumentException("Duplicate Paradise Lost score: " + points.get(i).score());
            }
        }
        return List.copyOf(points);
    }

    private record Point(int score, double bonus) {
    }
}
