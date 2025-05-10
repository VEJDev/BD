import pygame
import math
import random
import matplotlib
import matplotlib.pyplot as plt
from collections import namedtuple, deque
from itertools import count
import csv
import numpy as np
import environments
import sys

import torch
import torch.nn as nn
import torch.optim as optim
import torch.nn.functional as F

is_ipython = 'inline' in matplotlib.get_backend()
if is_ipython:
    from IPython import display

plt.ion()

device = torch.device(
    "cuda" if torch.cuda.is_available() else
    "mps" if torch.backends.mps.is_available() else
    "cpu"
)

Transition = namedtuple('Transition',
                        ('state', 'action', 'next_state', 'reward'))

class ReplayMemory(object):

    def __init__(self, capacity):
        self.memory = deque([], maxlen=capacity)

    def push(self, *args):
        self.memory.append(Transition(*args))

    def sample(self, batch_size):
        return random.sample(self.memory, batch_size)

    def __len__(self):
        return len(self.memory)
    
class DQN(nn.Module):

    def __init__(self, n_observations, n_actions):
        super(DQN, self).__init__()
        self.layer1 = nn.Linear(n_observations, 128)
        self.layer2 = nn.Linear(128, 128)
        self.layer3 = nn.Linear(128, n_actions)

    def forward(self, x):
        x = F.relu(self.layer1(x))
        x = F.relu(self.layer2(x))
        return self.layer3(x)
    
env = environments.Environment1()
pygame.init()
font = pygame.font.Font('arial.ttf', 25)

WHITE = (255, 255, 255)
RED = (200, 0, 0)
BLACK = (0, 0, 0)

SPEED = 1000

action_list = [
    [0, 0, 0, 0],
    [1, 0, 0, 0],
    [0, 1, 0, 0],
    [0, 0, 1, 0],
    [0, 0, 0, 1],
    [1, 1, 0, 0],
    [0, 1, 1, 0],
    [0, 0, 1, 1],
    [1, 0, 0, 1],
]

class BoatSimulation:
    
    history = []
    best_time = sys.maxsize
    
    def __init__(self, w=1280, h=720):
        self.w = w
        self.h = h
        self.display = pygame.display.set_mode((self.w, self.h), pygame.RESIZABLE)
        pygame.display.set_caption('Simulation')
        self.clock = pygame.time.Clock()

        self.reset()

        self.offset_x = -self.w // 2
        self.offset_y = self.h // 2
        self.scale = 10
        self.dragging = False
        self.last_mouse_pos = (0, 0)

    def reset(self):
        self.posX = env.initialState[0]
        self.posY = env.initialState[1]
        self.motionX = env.initialState[2]
        self.motionY = env.initialState[3]
        self.yaw = env.initialState[4]
        self.deltaYaw = env.initialState[5]
        self.time = 0
        self.history = []
        self.reward = 0
        self.cumulativeReward = 0
        return [self.posX, self.posY, self.motionX, self.motionY, self.yaw, self.deltaYaw]

    def play_step(self, action):
        action = self.convertAction(action)
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                pygame.quit()
                quit()

            elif event.type == pygame.MOUSEBUTTONDOWN:
                if event.button == 4:
                    self.scale *= 1.1
                elif event.button == 5:
                    self.scale /= 1.1
                elif event.button == 1:
                    self.dragging = True
                    self.last_mouse_pos = pygame.mouse.get_pos()
                elif event.button == 3:
                    self.offset_x = -self.w // 2
                    self.offset_y = self.h // 2

            elif event.type == pygame.MOUSEBUTTONUP:
                if event.button == 1:
                    self.dragging = False

            elif event.type == pygame.MOUSEMOTION:
                if self.dragging:
                    mx, my = pygame.mouse.get_pos()
                    dx, dy = mx - self.last_mouse_pos[0], my - self.last_mouse_pos[1]
                    self.offset_x += dx
                    self.offset_y += dy
                    self.last_mouse_pos = (mx, my)

        distToGoalBeforePhysics = env.approximateDistancetoFinish(self.posX, self.posY)

        # motion physics
        self.motionX *= 0.989
        self.motionY *= 0.989
        self.deltaYaw *= 0.989
        f = 0.0

        if action[0]:
            self.deltaYaw -= 1
        if action[2]:
            self.deltaYaw += 1
        if action[0] != action[2] and not action[1] and not action[3]:
            f = 0.005
        if action[1]:
            f = 0.04
        if action[3]:
            f = -0.005

        self.yaw += self.deltaYaw
        self.motionX += math.sin(math.radians(self.yaw)) * f
        self.motionY += math.cos(math.radians(self.yaw)) * f
        self.posX += self.motionX
        self.posY += self.motionY
        distToGoalAfterPhysics = env.approximateDistancetoFinish(self.posX, self.posY)

        self.time += 1

        self.history.append(action + [self.posX, self.posY])

        if not env.isValid(self.posX, self.posY):
            return [self.posX, self.posY, self.motionX, self.motionY, self.yaw, self.deltaYaw], -100, True, False
        if (distToGoalBeforePhysics - distToGoalAfterPhysics) > 10: # wrong direction
            return [self.posX, self.posY, self.motionX, self.motionY, self.yaw, self.deltaYaw], -100, True, False
        
        self.reward = (distToGoalBeforePhysics - distToGoalAfterPhysics) - 0.1 + min(env.distanceToConstraint(self.posX, self.posY)/2-1, 0)
        self.cumulativeReward += self.reward

        if env.isGoal(self.posX, self.posY):
            print("Completed in: ", self.time)
            if self.best_time > self.time:
                self.best_time = self.time
                self.updateBestPerformance()
            return [self.posX, self.posY, self.motionX, self.motionY, self.yaw, self.deltaYaw], 100000, True, False
        if self.time > env.maxSteps:
            return [self.posX, self.posY, self.motionX, self.motionY, self.yaw, self.deltaYaw], self.reward, False, True
        
        self._update_ui()
        self.clock.tick(SPEED)
        
        return [self.posX, self.posY, self.motionX, self.motionY, self.yaw, self.deltaYaw], self.reward, False, False
    
    def convertAction(self, action):
        return action_list[action]

    def draw_arrow(self, surface, color, start, angle, length=100, arrow_size=10, width=5):
        end_x = start[0] + length * math.cos(math.radians(angle))
        end_y = start[1] + length * math.sin(math.radians(angle))
        end = (end_x, end_y)

        pygame.draw.line(surface, color, start, end, width)

        left_x = end_x - arrow_size * math.cos(math.radians(angle - 30))
        left_y = end_y - arrow_size * math.sin(math.radians(angle - 30))
        right_x = end_x - arrow_size * math.cos(math.radians(angle + 30))
        right_y = end_y - arrow_size * math.sin(math.radians(angle + 30))

        pygame.draw.polygon(surface, color, [(end_x, end_y), (left_x, left_y), (right_x, right_y)])

    def _update_ui(self):
        self.display.fill(BLACK)

        env.drawEnvironment(self.w, self.scale, self.offset_x, self.offset_y, self.display)

        boat_x = self.w - (self.posX * self.scale) + self.offset_x
        boat_y = int(self.offset_y + self.posY * self.scale)
        self.draw_arrow(self.display, RED, (boat_x, boat_y), self.yaw + 90, 20 * self.scale / 10, 10 * self.scale / 10, 2)

        text = font.render(f"Time: {self.time}", True, WHITE)
        self.display.blit(text, [10, 10])
        text = font.render(f"Reward: {self.reward}", True, WHITE)
        self.display.blit(text, [10, 40])
        text = font.render(f"Cumulative: {self.cumulativeReward}", True, WHITE)
        self.display.blit(text, [10, 70])

        pygame.display.flip()

    def updateBestPerformance(self):
        with open("output.csv", mode='w', newline='') as file:
            writer = csv.writer(file)
            for i in self.history:
                writer.writerow(i)

BATCH_SIZE = 500
GAMMA = 0.99
EPS_START = 0.9
EPS_END = 0.05
EPS_DECAY = 1000
TAU = 0.001
LR = 1e-5

n_actions = 9
game = BoatSimulation()
state = game.reset()
n_observations = len(state)

policy_net = DQN(n_observations, n_actions).to(device)
target_net = DQN(n_observations, n_actions).to(device)
target_net.load_state_dict(policy_net.state_dict())

optimizer = optim.AdamW(policy_net.parameters(), lr=LR, amsgrad=True)
memory = ReplayMemory(10000)

steps_done = 0

def select_action(state):
    global steps_done
    sample = random.random()
    eps_threshold = EPS_END + (EPS_START - EPS_END) * \
        math.exp(-1. * steps_done / EPS_DECAY)
    steps_done += 1
    if sample > eps_threshold:
        with torch.no_grad():
            return policy_net(state).max(1).indices.view(1, 1)
    else:
        return torch.tensor([[random.randint(0, 8)]], device=device, dtype=torch.long)


episode_durations = []


def plot_durations(show_result=False):
    plt.figure(1)
    durations_t = torch.tensor(episode_durations, dtype=torch.float)
    if show_result:
        plt.title('Rezultāts')
    else:
        plt.clf()
        plt.title('Apmācība...')
    plt.xlabel('Epizode')
    plt.ylabel('Ilgums')
    plt.plot(durations_t.numpy())
    if len(durations_t) >= 100:
        means = durations_t.unfold(0, 100, 1).mean(1).view(-1)
        means = torch.cat((torch.zeros(99), means))
        plt.plot(means.numpy())

    plt.pause(0.001)
    if is_ipython:
        if not show_result:
            display.display(plt.gcf())
            display.clear_output(wait=True)
        else:
            display.display(plt.gcf())

def optimize_model():
    if len(memory) < BATCH_SIZE:
        return
    transitions = memory.sample(BATCH_SIZE)
    batch = Transition(*zip(*transitions))

    non_final_mask = torch.tensor(tuple(map(lambda s: s is not None,
                                          batch.next_state)), device=device, dtype=torch.bool)
    non_final_next_states = torch.cat([s for s in batch.next_state
                                                if s is not None])
    state_batch = torch.cat(batch.state)
    action_batch = torch.cat(batch.action)
    reward_batch = torch.cat(batch.reward)

    state_action_values = policy_net(state_batch).gather(1, action_batch)

    next_state_values = torch.zeros(BATCH_SIZE, device=device)
    with torch.no_grad():
        next_state_values[non_final_mask] = target_net(non_final_next_states).max(1).values
    expected_state_action_values = (next_state_values * GAMMA) + reward_batch

    criterion = nn.SmoothL1Loss()
    loss = criterion(state_action_values, expected_state_action_values.unsqueeze(1))

    optimizer.zero_grad()
    loss.backward()
    torch.nn.utils.clip_grad_value_(policy_net.parameters(), 100)
    optimizer.step()

if torch.cuda.is_available() or torch.backends.mps.is_available():
    num_episodes = 1000
else:
    num_episodes = 50

for i_episode in range(num_episodes):
    state = game.reset()
    state = torch.tensor(state, dtype=torch.float32, device=device).unsqueeze(0)
    for t in count():
        action = select_action(state)
        observation, reward, terminated, truncated = game.play_step(action.item())
        reward = torch.tensor([reward], device=device)
        done = terminated or truncated

        if terminated:
            next_state = None
        else:
            next_state = torch.tensor(observation, dtype=torch.float32, device=device).unsqueeze(0)
        memory.push(state, action, next_state, reward)
        state = next_state
        optimize_model()
        target_net_state_dict = target_net.state_dict()
        policy_net_state_dict = policy_net.state_dict()
        for key in policy_net_state_dict:
            target_net_state_dict[key] = policy_net_state_dict[key]*TAU + target_net_state_dict[key]*(1-TAU)
        target_net.load_state_dict(target_net_state_dict)

        if done:
            episode_durations.append(t + 1)
            plot_durations()
            break

print('Complete')
plot_durations(show_result=True)
plt.ioff()
plt.show()