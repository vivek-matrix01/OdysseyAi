import avatarDog from '../assets/profile/charlesdeluvio-K4mSJ7kc0As-unsplash.jpg';
import avatarRaccoon from '../assets/profile/gary-bendig-6GMq7AGxNbE-unsplash.jpg';
import avatarCyber from '../assets/profile/mateusz-d-BWjuyBJEHdQ-unsplash (1).jpg';
import avatarLion from '../assets/profile/ricky-kharawala-adK3Vu70DEQ-unsplash.jpg';
import avatarSunset from '../assets/profile/tuqa-nabi-uhnZZUaTIOs-unsplash.jpg';

export interface ProfileAvatarOption {
  id: string;
  name: string;
  subtitle: string;
  src: string;
  filename: string;
}

export const PROFILE_AVATARS: ProfileAvatarOption[] = [
  {
    id: 'cool-dog',
    name: 'Cool Explorer',
    subtitle: 'Urban & stylish vibe',
    src: avatarDog,
    filename: 'charlesdeluvio-K4mSJ7kc0As-unsplash.jpg',
  },
  {
    id: 'nature-raccoon',
    name: 'Curious Wanderer',
    subtitle: 'Nature & wilderness',
    src: avatarRaccoon,
    filename: 'gary-bendig-6GMq7AGxNbE-unsplash.jpg',
  },
  {
    id: 'neon-cyber',
    name: 'Neon Voyager',
    subtitle: 'Cyberpunk & night city',
    src: avatarCyber,
    filename: 'mateusz-d-BWjuyBJEHdQ-unsplash (1).jpg',
  },
  {
    id: 'majestic-lion',
    name: 'Majestic Nomad',
    subtitle: 'Bold & fearless travel',
    src: avatarLion,
    filename: 'ricky-kharawala-adK3Vu70DEQ-unsplash.jpg',
  },
  {
    id: 'sunset-silhouette',
    name: 'Sunset Dreamer',
    subtitle: 'Peaceful horizons',
    src: avatarSunset,
    filename: 'tuqa-nabi-uhnZZUaTIOs-unsplash.jpg',
  },
];

export const getAvatarSrc = (picturePathOrId?: string | null): string | null => {
  if (!picturePathOrId) return null;
  const match = PROFILE_AVATARS.find(
    (a) =>
      a.id === picturePathOrId ||
      a.filename === picturePathOrId ||
      picturePathOrId.includes(a.id) ||
      picturePathOrId.includes(a.filename)
  );
  if (match) return match.src;
  if (picturePathOrId.startsWith('http') || picturePathOrId.startsWith('/')) {
    return picturePathOrId;
  }
  return null;
};
