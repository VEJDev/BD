import math
import csv
import pygame

class EnvironmentBase:
    
    maxSteps = 20
    
    def __init__(self):
        self.initialState = (0, 0, 0, 0, 0, 0)
        self.goalRegion = Location(0, 0, 0, 0)

    def isValid(self, x, y):
        return True
    
    def isGoal(self, x, y):
        return self.goalRegion.contains(x, y)
    
    def approximateDistancetoFinish(self, x, y):
        dx = 0
        if (x <= self.goalRegion.min_x or x >= self.goalRegion.max_x):
            dx = min(abs(self.goalRegion.min_x - x), abs(self.goalRegion.max_x - x))
        dy = 0
        if (y <= self.goalRegion.min_y or y >= self.goalRegion.max_y):
            dy = min(abs(self.goalRegion.min_y - y), abs(self.goalRegion.max_y - y))
        return math.sqrt(dx * dx + dy * dy)
    
    def drawEnvironment(self, w, scale, offset_x, offset_y, display):
        x = self.goalRegion.min_x
        y = self.goalRegion.min_y
        flipped_x = w - (x * scale) + offset_x - (self.goalRegion.max_x - self.goalRegion.min_x) * scale
        scaled_y = int(offset_y + y * scale)
        rect = pygame.Rect(flipped_x, scaled_y, (self.goalRegion.max_x - self.goalRegion.min_x) * scale, (self.goalRegion.max_y - self.goalRegion.min_y) * scale)
        pygame.draw.rect(display, (0, 255, 0), rect, width=1)

        x = self.initialState[0]
        y = self.initialState[1]
        flipped_x = w - (x * scale) + offset_x
        scaled_y = int(offset_y + y * scale)
        pygame.draw.circle(display, (255, 0, 255), (flipped_x, scaled_y), 5)

    def distanceToConstraint(self, x, y):
        return 999

class Environment1(EnvironmentBase):
    
    maxSteps = 200
    
    def __init__(self):
        super().__init__()
        self.initialState = (0, 0, 0, 0, 0, 0)
        self.goalRegion = Location(12.5, 12.5, 13, 13)

class Environment2(EnvironmentBase):
    
    maxSteps = 100
    
    def __init__(self):
        super().__init__()
        self.initialState = (0, 0, 0, 0, 0, 0)
        self.goalRegion = Location(-0.5, 5, 0.5, 20)

    def isValid(self, x, y):
        return x >= 0.2 or x <= -0.2 or y >= 2.0 or y < 1.0
    
    def distanceToConstraint(self, x, y):
        dx = max(-0.2 - x, 0, x - 0.2)
        dy = max(1.0 - y, 0, y - 2.0)
        return math.hypot(dx, dy)
    
    def drawEnvironment(self, w, scale, offset_x, offset_y, display):
        x = 0.2
        y = 1
        flipped_x = w - (x * scale) + offset_x
        scaled_y = int(offset_y + y * scale)
        rect = pygame.Rect(flipped_x, scaled_y, 0.4 * scale, 1 * scale)
        pygame.draw.rect(display, (255, 0, 0), rect, width=1)
        super().drawEnvironment(w, scale, offset_x, offset_y, display)

class Environment3(EnvironmentBase):
    
    maxSteps = 4000
    data = {}
    with open("C:\\Users\\user\\Desktop\\output2.csv", mode="r", newline="") as file:
        reader = csv.reader(file, delimiter=',')
        for row in reader:
            x, y, dist = map(float, row)
            data[(int(x), int(y))] = dist

    min_dist = min(data.values())
    max_dist = max(data.values())
    
    def __init__(self):
        super().__init__()
        self.initialState = (0, 0, 0, 0, -180.0, 0)
        self.goalRegion = Location(-4, 1, 3, 2)

    def isValid(self, x, y):
        x = int(x)
        y = int(y)
        if self.data.get((x, y)) is not None:
            return True
        else:
            return False
    
    def approximateDistancetoFinish(self, x, y):
        x = int(x)
        y = int(y)
        return self.data.get((x, y), None)
    
    def drawEnvironment(self, w, scale, offset_x, offset_y, display):
        for (x, y), dist in self.data.items():
            color = get_gradient_color(dist, self.min_dist, self.max_dist)
            flipped_x = w - (x * scale) + offset_x
            scaled_y = int(offset_y + y * scale)
            rect_size = int(scale)
            rect = pygame.Rect(flipped_x, scaled_y, rect_size, rect_size)
            pygame.draw.rect(display, color, rect, width=1)
        super().drawEnvironment(w, scale, offset_x, offset_y, display)

    def distanceToConstraint(self, x, y):
        return 999
    
class Location:
    
    def __init__(self, min_x, min_y, max_x, max_y):
        self.min_x = min_x
        self.min_y = min_y
        self.max_x = max_x
        self.max_y = max_y

    def contains(self, x, y):
        return self.min_x <= x and self.max_x >= x and self.min_y <= y and self.max_y >= y
    
def get_gradient_color(value, min_value, max_value):
    ratio = (value - min_value) / (max_value - min_value)
    r = int(255 * (1 - ratio))
    g = 0
    b = int(255 * ratio)
    return (r, g, b)